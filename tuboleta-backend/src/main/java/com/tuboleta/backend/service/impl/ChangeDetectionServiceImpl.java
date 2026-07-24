package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.EventChange;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.enums.EventStatus;
import com.tuboleta.backend.domain.enums.NotificationType;
import com.tuboleta.backend.repository.EventChangeRepository;
import com.tuboleta.backend.repository.EventRepository;
import com.tuboleta.backend.service.ChangeDetectionService;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.RawEventData;
import com.tuboleta.backend.utils.text.SpanishDateParser;
import com.tuboleta.backend.utils.text.TermNormalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la detección de cambios (REQ-DET-001/002/003 + filtro
 * REQ-BUS-003). Única responsabilidad: decidir NEW/CHANGED/REMOVED y
 * persistir {@code events}/{@code event_changes} — NO notifica (REQ-ARQ-004).
 */
@Service
public class ChangeDetectionServiceImpl implements ChangeDetectionService {

    private static final Logger log = LogManager.getLogger(ChangeDetectionServiceImpl.class);

    /**
     * Corridas exitosas consecutivas sin ver el evento antes de marcarlo
     * REMOVED (REQ-DET-003).
     */
    static final int MISS_THRESHOLD = 2;

    private final EventRepository eventRepository;
    private final EventChangeRepository eventChangeRepository;

    public ChangeDetectionServiceImpl(EventRepository eventRepository, EventChangeRepository eventChangeRepository) {
        this.eventRepository = eventRepository;
        this.eventChangeRepository = eventChangeRepository;
    }

    @Override
    @Transactional
    public List<DetectedChange> detect(SearchProvider pair, ExtractionResult extraction) {
        List<Event> existingEvents = eventRepository.findBySearchProviderId(pair.getId());
        Map<String, Event> byExternalId = existingEvents.stream()
                .collect(Collectors.toMap(Event::getExternalId, Function.identity()));
        String normalizedTerm = pair.getSearch().getTermNormalized();
        Instant now = Instant.now();

        List<DetectedChange> changes = new ArrayList<>();
        Set<String> seenExternalIds = new HashSet<>();
        List<RawEventData> dedupedItems = deduplicateByExternalId(extraction.items());

        for (RawEventData item : dedupedItems) {
            seenExternalIds.add(item.externalId());
            Event existing = byExternalId.get(item.externalId());
            if (existing == null) {
                processNewItem(pair, item, normalizedTerm, now, changes);
            } else if (existing.getStatus() == EventStatus.REMOVED) {
                processReappearance(existing, item, now, changes);
            } else {
                processKnownActiveItem(existing, item, now, changes);
            }
        }

        if (extraction.failedItems() == 0) {
            processAbsences(existingEvents, seenExternalIds, changes);
        } else {
            log.debug("Corrida parcial ({} ítems fallidos): ausencias no se procesan (REQ-DET-003)",
                    extraction.failedItems());
        }

        return changes;
    }

    /**
     * De-duplica {@code items} por {@code externalId}, conservando la
     * primera ocurrencia. Un mismo externalId repetido dentro de una misma
     * extracción (bug de la fuente) generaría dos inserts para el mismo
     * evento y violaría la constraint única {@code uq_event_per_search_provider},
     * abortando toda la transacción.
     */
    private List<RawEventData> deduplicateByExternalId(List<RawEventData> items) {
        Map<String, RawEventData> byExternalId = new LinkedHashMap<>();
        for (RawEventData item : items) {
            RawEventData previous = byExternalId.putIfAbsent(item.externalId(), item);
            if (previous != null) {
                log.warn("externalId duplicado '{}' en la misma extracción: se ignora la ocurrencia repetida",
                        item.externalId());
            }
        }
        return new ArrayList<>(byExternalId.values());
    }

    /**
     * Ítem sin fila previa en {@code events}: aplica el filtro REQ-BUS-003 —
     * solo pasa si el título contiene el término normalizado. Los que no
     * pasan se descartan sin dejar rastro.
     */
    private void processNewItem(SearchProvider pair, RawEventData item, String normalizedTerm,
                                 Instant now, List<DetectedChange> changes) {
        if (!TermNormalizer.matches(item.title(), normalizedTerm)) {
            return;
        }
        Event event = Event.builder()
                .searchProvider(pair)
                .externalId(item.externalId())
                .title(item.title())
                .venue(item.venue())
                .eventDateRaw(item.eventDateRaw())
                .eventDate(SpanishDateParser.parse(item.eventDateRaw()).orElse(null))
                .rawJson(item.rawJson())
                .status(EventStatus.ACTIVE)
                .missCount(0)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .build();
        eventRepository.save(event);
        changes.add(new DetectedChange(NotificationType.NEW, event));
    }

    /**
     * Ítem conocido que estaba REMOVED: reaparición (REQ-DET-001). Vuelve a
     * ACTIVE con miss_count=0, se registran en {@code event_changes} los
     * campos que hayan cambiado durante la ausencia, y se notifica NEW (no
     * CHANGED adicional).
     */
    private void processReappearance(Event existing, RawEventData item, Instant now, List<DetectedChange> changes) {
        List<EventChange> fieldChanges = diffFields(existing, item, now);
        if (!fieldChanges.isEmpty()) {
            eventChangeRepository.saveAll(fieldChanges);
        }
        applyFields(existing, item);
        existing.setStatus(EventStatus.ACTIVE);
        existing.setMissCount(0);
        existing.setLastSeenAt(now);
        eventRepository.save(existing);
        changes.add(new DetectedChange(NotificationType.NEW, existing));
    }

    /**
     * Ítem conocido que estaba ACTIVE: diff campo a campo (REQ-DET-002).
     * SIEMPRE se procesa aunque el título ya no coincida con el término
     * (REQ-BUS-003: el filtro solo aplica a ítems nuevos).
     */
    private void processKnownActiveItem(Event existing, RawEventData item, Instant now, List<DetectedChange> changes) {
        List<EventChange> fieldChanges = diffFields(existing, item, now);
        existing.setMissCount(0);
        existing.setLastSeenAt(now);
        if (!fieldChanges.isEmpty()) {
            eventChangeRepository.saveAll(fieldChanges);
            applyFields(existing, item);
            eventRepository.save(existing);
            changes.add(new DetectedChange(NotificationType.CHANGED, existing));
        } else {
            eventRepository.save(existing);
        }
    }

    /**
     * Eventos ACTIVE del par que no vinieron en esta corrida (REQ-DET-003).
     * Solo se invoca cuando la corrida fue completamente exitosa
     * ({@code failedItems == 0}); nunca sobre una corrida parcial.
     */
    private void processAbsences(List<Event> existingEvents, Set<String> seenExternalIds,
                                  List<DetectedChange> changes) {
        for (Event event : existingEvents) {
            if (event.getStatus() != EventStatus.ACTIVE || seenExternalIds.contains(event.getExternalId())) {
                continue;
            }
            int newMissCount = event.getMissCount() + 1;
            event.setMissCount(newMissCount);
            if (newMissCount >= MISS_THRESHOLD) {
                event.setStatus(EventStatus.REMOVED);
                changes.add(new DetectedChange(NotificationType.REMOVED, event));
            }
            eventRepository.save(event);
        }
    }

    /**
     * Diff campo a campo de las columnas extraídas ({@code title},
     * {@code venue}, {@code eventDateRaw}) — {@code eventDate} es derivado,
     * no participa (REQ-DET-001).
     */
    private List<EventChange> diffFields(Event existing, RawEventData item, Instant now) {
        List<EventChange> result = new ArrayList<>();
        addIfChanged(result, existing, "title", existing.getTitle(), item.title(), now);
        addIfChanged(result, existing, "venue", existing.getVenue(), item.venue(), now);
        addIfChanged(result, existing, "eventDateRaw", existing.getEventDateRaw(), item.eventDateRaw(), now);
        return result;
    }

    private void addIfChanged(List<EventChange> result, Event event, String fieldName,
                               String oldValue, String newValue, Instant now) {
        if (!Objects.equals(oldValue, newValue)) {
            result.add(EventChange.builder()
                    .event(event)
                    .fieldName(fieldName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .detectedAt(now)
                    .build());
        }
    }

    private void applyFields(Event event, RawEventData item) {
        event.setTitle(item.title());
        event.setVenue(item.venue());
        event.setEventDateRaw(item.eventDateRaw());
        event.setEventDate(SpanishDateParser.parse(item.eventDateRaw()).orElse(null));
        event.setRawJson(item.rawJson());
    }
}
