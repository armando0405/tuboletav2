package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.EventChangeResponse;
import com.tuboleta.backend.api.dtos.EventResponse;
import com.tuboleta.backend.api.dtos.SearchCreateRequest;
import com.tuboleta.backend.api.dtos.SearchProviderInfo;
import com.tuboleta.backend.api.dtos.SearchResponse;
import com.tuboleta.backend.api.dtos.SearchUpdateRequest;
import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.EventChange;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchNotification;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.enums.SearchStatus;
import com.tuboleta.backend.repository.EventChangeRepository;
import com.tuboleta.backend.repository.EventRepository;
import com.tuboleta.backend.repository.FrequencyRepository;
import com.tuboleta.backend.repository.ProviderRepository;
import com.tuboleta.backend.repository.SearchNotificationRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.repository.SearchRepository;
import com.tuboleta.backend.repository.UserNotificationChannelRepository;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.SearchService;
import com.tuboleta.backend.service.scheduler.DueGroup;
import com.tuboleta.backend.service.scheduler.MonitoringRunService;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.GenericException;
import com.tuboleta.backend.utils.exception.NotFoundRegisterException;
import com.tuboleta.backend.utils.text.TermNormalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ciclo de vida de una búsqueda (REQ-BUS-001/002/004/005): normalización +
 * unicidad del término, pares con proveedores y fan-out de destinos.
 * Ownership: todo lo ajeno se trata como inexistente (NotFoundRegisterException).
 */
@Service
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;
    private final SearchProviderRepository searchProviderRepository;
    private final SearchNotificationRepository searchNotificationRepository;
    private final ProviderRepository providerRepository;
    private final UserNotificationChannelRepository destinationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MonitoringRunService monitoringRunService;
    private final FrequencyRepository frequencyRepository;
    private final EventChangeRepository eventChangeRepository;

    public SearchServiceImpl(SearchRepository searchRepository,
                              SearchProviderRepository searchProviderRepository,
                              SearchNotificationRepository searchNotificationRepository,
                              ProviderRepository providerRepository,
                              UserNotificationChannelRepository destinationRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository,
                              MonitoringRunService monitoringRunService,
                              FrequencyRepository frequencyRepository,
                              EventChangeRepository eventChangeRepository) {
        this.searchRepository = searchRepository;
        this.searchProviderRepository = searchProviderRepository;
        this.searchNotificationRepository = searchNotificationRepository;
        this.providerRepository = providerRepository;
        this.destinationRepository = destinationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.monitoringRunService = monitoringRunService;
        this.frequencyRepository = frequencyRepository;
        this.eventChangeRepository = eventChangeRepository;
    }

    /**
     * Valida que una frecuencia (en minutos) exista y esté activa en el
     * catálogo {@code frequencies}. Antes eran presets fijos en el DTO; ahora
     * el catálogo es dinámico (gestionado por el admin), así que la validación
     * vive aquí donde hay acceso a la BD.
     */
    private void validateFrequency(Integer minutes) {
        if (minutes == null) {
            return;
        }
        frequencyRepository.findByMinutesAndIsActiveTrue(minutes)
                .orElseThrow(() -> new GenericException(HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_FREQUENCY));
    }

    @Override
    @Transactional
    public SearchResponse create(Long userId, SearchCreateRequest request) {
        validateFrequency(request.checkFrequencyMinutes());
        String normalized = TermNormalizer.normalize(request.term());
        searchRepository.findByUserIdAndTermNormalizedAndStatusNot(userId, normalized, SearchStatus.DELETED)
                .ifPresent(existing -> {
                    throw new GenericException(HttpStatus.CONFLICT, ErrorMessage.SEARCH_DUPLICATE, request.term());
                });

        Search search = Search.builder()
                .user(userRepository.getReferenceById(userId))
                .term(request.term())
                .termNormalized(normalized)
                .checkFrequencyMinutes(request.checkFrequencyMinutes())
                .status(SearchStatus.ACTIVE)
                .build();
        search = searchRepository.save(search);

        Set<Long> providerIds = new LinkedHashSet<>(request.providerIds());
        for (Long providerId : providerIds) {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new NotFoundRegisterException("Proveedor", "id", providerId));
            SearchProvider pair = SearchProvider.builder()
                    .search(search)
                    .provider(provider)
                    .isActive(true)
                    .build();
            searchProviderRepository.save(pair);
        }

        List<Long> requestedDestinationIds = request.destinationIds() == null ? List.of() : request.destinationIds();
        Set<Long> destinationIds = new LinkedHashSet<>(requestedDestinationIds);
        for (Long destinationId : destinationIds) {
            var destination = destinationRepository.findByIdAndUserId(destinationId, userId)
                    .orElseThrow(() -> new NotFoundRegisterException("Destino", "id", destinationId));
            SearchNotification searchNotification = SearchNotification.builder()
                    .search(search)
                    .userNotificationChannel(destination)
                    .isActive(true)
                    .build();
            searchNotificationRepository.save(searchNotification);
        }

        return toResponse(search);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchResponse> listMine(Long userId) {
        return searchRepository.findByUserIdAndStatusNot(userId, SearchStatus.DELETED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> events(Long userId, Long searchId) {
        ownSearch(userId, searchId);
        List<Long> pairIds = searchProviderRepository.findBySearchId(searchId).stream()
                .map(SearchProvider::getId)
                .toList();
        if (pairIds.isEmpty()) {
            return List.of();
        }
        List<Event> events = eventRepository.findBySearchProviderIdInOrderByLastSeenAtDesc(pairIds);
        Map<Long, Long> changeCounts = changeCountsByEvent(events);
        return events.stream()
                .map(event -> toEventResponse(event, changeCounts.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    /** Cambios (event_changes) de un evento de la búsqueda, más recientes primero (REQ-DET-002). */
    @Override
    @Transactional(readOnly = true)
    public List<EventChangeResponse> eventChanges(Long userId, Long searchId, Long eventId) {
        ownSearch(userId, searchId);
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getSearchProvider().getSearch().getId().equals(searchId))
                .orElseThrow(() -> new NotFoundRegisterException("Evento", "id", eventId));
        return eventChangeRepository.findByEventIdOrderByDetectedAtDesc(event.getId()).stream()
                .map(SearchServiceImpl::toChangeResponse)
                .toList();
    }

    /** Conteo de cambios por evento en una sola consulta (evita N+1). */
    private Map<Long, Long> changeCountsByEvent(List<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : eventChangeRepository.countGroupedByEventIds(ids)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    @Transactional
    public SearchResponse update(Long userId, Long searchId, SearchUpdateRequest request) {
        Search search = ownSearch(userId, searchId);
        if (request.checkFrequencyMinutes() != null) {
            validateFrequency(request.checkFrequencyMinutes());
            search.setCheckFrequencyMinutes(request.checkFrequencyMinutes());
        }
        if (request.destinationIds() != null) {
            reconcileDestinations(userId, search, request.destinationIds());
        }
        search = searchRepository.save(search);
        return toResponse(search);
    }

    @Override
    @Transactional
    public SearchResponse togglePair(Long userId, Long searchId, Long providerId) {
        Search search = ownSearch(userId, searchId);
        SearchProvider pair = searchProviderRepository.findBySearchIdAndProviderId(searchId, providerId)
                .orElseThrow(() -> new NotFoundRegisterException("Proveedor de la busqueda", "providerId", providerId));
        pair.setIsActive(!Boolean.TRUE.equals(pair.getIsActive()));
        searchProviderRepository.save(pair);
        return toResponse(search);
    }

    @Override
    @Transactional
    public SearchResponse toggleStatus(Long userId, Long searchId) {
        Search search = ownSearch(userId, searchId);
        if (search.getStatus() == SearchStatus.DELETED) {
            throw new NotFoundRegisterException("Busqueda", "id", searchId);
        }
        search.setStatus(search.getStatus() == SearchStatus.ACTIVE ? SearchStatus.INACTIVE : SearchStatus.ACTIVE);
        search = searchRepository.save(search);
        return toResponse(search);
    }

    /**
     * NO es {@code @Transactional} a propósito: replica el camino del
     * scheduler, donde {@link MonitoringRunService#runGroup} hace la
     * extracción HTTP fuera de transacción y delega la persistencia a sus
     * propios métodos transaccionales. Envolver esto en una transacción
     * retendría una conexión JDBC durante el scraping.
     */
    @Override
    public long runNow(Long userId, Long searchId) {
        Search search = ownSearch(userId, searchId);
        if (search.getStatus() == SearchStatus.DELETED) {
            throw new NotFoundRegisterException("Busqueda", "id", searchId);
        }
        List<SearchProvider> pairs = searchProviderRepository.findActivePairsForRunNow(searchId);
        Map<Long, List<SearchProvider>> byProvider = pairs.stream()
                .collect(Collectors.groupingBy(p -> p.getProvider().getId(), LinkedHashMap::new, Collectors.toList()));
        for (List<SearchProvider> group : byProvider.values()) {
            DueGroup dueGroup = new DueGroup(group.get(0).getProvider(), search.getTermNormalized(), group);
            monitoringRunService.runGroup(dueGroup);
        }
        List<Long> pairIds = searchProviderRepository.findBySearchId(searchId).stream()
                .map(SearchProvider::getId)
                .toList();
        return pairIds.isEmpty() ? 0L : eventRepository.countBySearchProviderIdIn(pairIds);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long searchId) {
        Search search = ownSearch(userId, searchId);
        search.setStatus(SearchStatus.DELETED);
        searchRepository.save(search);
    }

    private Search ownSearch(Long userId, Long searchId) {
        return searchRepository.findByIdAndUserId(searchId, userId)
                .orElseThrow(() -> new NotFoundRegisterException("Busqueda", "id", searchId));
    }

    private void reconcileDestinations(Long userId, Search search, List<Long> destinationIds) {
        Set<Long> desired = new HashSet<>(destinationIds);
        for (Long destinationId : desired) {
            destinationRepository.findByIdAndUserId(destinationId, userId)
                    .orElseThrow(() -> new NotFoundRegisterException("Destino", "id", destinationId));
        }

        List<SearchNotification> current = searchNotificationRepository.findBySearchId(search.getId());
        Set<Long> alreadyLinked = new HashSet<>();
        for (SearchNotification link : current) {
            Long channelId = link.getUserNotificationChannel().getId();
            alreadyLinked.add(channelId);
            boolean shouldBeActive = desired.contains(channelId);
            if (!Boolean.valueOf(shouldBeActive).equals(link.getIsActive())) {
                link.setIsActive(shouldBeActive);
                searchNotificationRepository.save(link);
            }
        }
        for (Long destinationId : desired) {
            if (alreadyLinked.contains(destinationId)) {
                continue;
            }
            var destination = destinationRepository.findByIdAndUserId(destinationId, userId).orElseThrow();
            SearchNotification link = SearchNotification.builder()
                    .search(search)
                    .userNotificationChannel(destination)
                    .isActive(true)
                    .build();
            searchNotificationRepository.save(link);
        }
    }

    private SearchResponse toResponse(Search search) {
        List<SearchProvider> pairs = searchProviderRepository.findBySearchId(search.getId());
        List<SearchProviderInfo> providers = pairs.stream()
                .map(pair -> new SearchProviderInfo(
                        pair.getProvider().getId(),
                        pair.getProvider().getName(),
                        pair.getIsActive(),
                        pair.getProvider().getStatus(),
                        pair.getProvider().getStatusReason(),
                        pair.getLastRunAt()))
                .toList();
        List<Long> destinationIds = searchNotificationRepository.findBySearchIdAndIsActiveTrue(search.getId()).stream()
                .map(link -> link.getUserNotificationChannel().getId())
                .toList();
        List<Long> pairIds = pairs.stream().map(SearchProvider::getId).toList();
        long eventsCount = pairIds.isEmpty() ? 0L : eventRepository.countBySearchProviderIdIn(pairIds);
        return new SearchResponse(search.getId(), search.getTerm(), search.getCheckFrequencyMinutes(),
                search.getStatus(), providers, destinationIds, eventsCount);
    }

    private static EventResponse toEventResponse(Event event, long changesCount) {
        return new EventResponse(
                event.getId(),
                event.getSearchProvider().getProvider().getName(),
                event.getExternalId(),
                event.getTitle(),
                event.getVenue(),
                event.getEventDateRaw(),
                event.getEventDate(),
                event.getStatus(),
                event.getMissCount(),
                event.getFirstSeenAt(),
                event.getLastSeenAt(),
                changesCount);
    }

    private static EventChangeResponse toChangeResponse(EventChange change) {
        return new EventChangeResponse(
                change.getId(),
                change.getFieldName(),
                fieldLabel(change.getFieldName()),
                change.getOldValue(),
                change.getNewValue(),
                change.getDetectedAt());
    }

    /** Etiqueta en español del campo que cambió (los que diffea la detección, REQ-DET-002). */
    private static String fieldLabel(String fieldName) {
        return switch (fieldName == null ? "" : fieldName) {
            case "title" -> "Título";
            case "venue" -> "Lugar";
            case "eventDateRaw" -> "Fecha";
            default -> fieldName;
        };
    }
}
