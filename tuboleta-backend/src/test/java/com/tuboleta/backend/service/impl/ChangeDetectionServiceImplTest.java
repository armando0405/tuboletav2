package com.tuboleta.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.EventChange;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.enums.EventStatus;
import com.tuboleta.backend.domain.enums.NotificationType;
import com.tuboleta.backend.repository.EventChangeRepository;
import com.tuboleta.backend.repository.EventRepository;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.RawEventData;
import com.tuboleta.backend.utils.text.SpanishDateParser;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos mínimos del brief de Task 4, uno por regla de
 * REQ-DET-001/002/003 + filtro REQ-BUS-003.
 */
@ExtendWith(MockitoExtension.class)
class ChangeDetectionServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventChangeRepository eventChangeRepository;

    private ChangeDetectionServiceImpl service;

    private SearchProvider pair;

    @BeforeEach
    void setUp() {
        service = new ChangeDetectionServiceImpl(eventRepository, eventChangeRepository);
        Search search = Search.builder().id(1L).termNormalized("rock fest").build();
        pair = SearchProvider.builder().id(10L).search(search).build();
    }

    private Event activeEvent(String externalId, String title, String venue, String eventDateRaw, int missCount) {
        return Event.builder()
                .id(100L)
                .searchProvider(pair)
                .externalId(externalId)
                .title(title)
                .venue(venue)
                .eventDateRaw(eventDateRaw)
                .eventDate(SpanishDateParser.parse(eventDateRaw).orElse(null))
                .rawJson("{}")
                .status(EventStatus.ACTIVE)
                .missCount(missCount)
                .firstSeenAt(Instant.now().minusSeconds(3600))
                .lastSeenAt(Instant.now().minusSeconds(3600))
                .build();
    }

    @Test
    void newItemThatMatchesTerm_isPersistedAsNewWithParsedEventDate() {
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of());
        RawEventData item = new RawEventData("ext1", "Rock Fest 2026", "Movistar Arena", "Bogota", "5 Jul", "{\"a\":1}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).type()).isEqualTo(NotificationType.NEW);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getExternalId()).isEqualTo("ext1");
        assertThat(saved.getTitle()).isEqualTo("Rock Fest 2026");
        assertThat(saved.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(saved.getMissCount()).isZero();
        assertThat(saved.getEventDate()).isEqualTo(SpanishDateParser.parse("5 Jul").orElseThrow());
        assertThat(changes.get(0).event()).isSameAs(saved);
    }

    @Test
    void newItemThatDoesNotMatchTerm_isDiscardedWithoutTrace() {
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of());
        RawEventData item = new RawEventData("ext1", "Festival de Jazz", "Teatro Colon", "Bogota", "5 Jul", "{}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).isEmpty();
        verify(eventRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventChangeRepository, never()).saveAll(anyList());
    }

    @Test
    void knownItemWithChangedTitle_producesOneChangedAndOneEventChangeRow() {
        Event existing = activeEvent("ext1", "Old Title", "Movistar Arena", "5 Jul", 0);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));
        RawEventData item = new RawEventData("ext1", "New Title", "Movistar Arena", "Bogota", "5 Jul", "{}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).type()).isEqualTo(NotificationType.CHANGED);
        assertThat(changes.get(0).event()).isSameAs(existing);
        assertThat(existing.getTitle()).isEqualTo("New Title");
        assertThat(existing.getMissCount()).isZero();

        ArgumentCaptor<List<EventChange>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventChangeRepository).saveAll(captor.capture());
        List<EventChange> fieldChanges = captor.getValue();
        assertThat(fieldChanges).hasSize(1);
        assertThat(fieldChanges.get(0).getFieldName()).isEqualTo("title");
        assertThat(fieldChanges.get(0).getOldValue()).isEqualTo("Old Title");
        assertThat(fieldChanges.get(0).getNewValue()).isEqualTo("New Title");
    }

    @Test
    void knownItemWithChangedTitleAndVenue_producesOneChangedAndTwoEventChangeRows() {
        Event existing = activeEvent("ext1", "Old Title", "Old Venue", "5 Jul", 0);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));
        RawEventData item = new RawEventData("ext1", "New Title", "New Venue", "Bogota", "5 Jul", "{}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).type()).isEqualTo(NotificationType.CHANGED);

        ArgumentCaptor<List<EventChange>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventChangeRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue())
                .extracting(EventChange::getFieldName)
                .containsExactlyInAnyOrder("title", "venue");
    }

    @Test
    void knownItemWithNoChanges_producesNoDetectedChangeButUpdatesLastSeen() {
        Instant before = Instant.now();
        Event existing = activeEvent("ext1", "Same Title", "Same Venue", "5 Jul", 0);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));
        RawEventData item = new RawEventData("ext1", "Same Title", "Same Venue", "Bogota", "5 Jul", "{}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).isEmpty();
        verify(eventChangeRepository, never()).saveAll(anyList());
        assertThat(existing.getMissCount()).isZero();
        assertThat(existing.getLastSeenAt()).isAfterOrEqualTo(before);
    }

    @Test
    void absentItemInSuccessfulRun_incrementsMissCountWithoutNotification() {
        Event existing = activeEvent("ext1", "Title", "Venue", "5 Jul", 0);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(), 0));

        assertThat(changes).isEmpty();
        assertThat(existing.getMissCount()).isEqualTo(1);
        assertThat(existing.getStatus()).isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    void secondConsecutiveAbsence_removesEvent_thirdAbsenceDoesNothingNew() {
        Event existing = activeEvent("ext1", "Title", "Venue", "5 Jul", 1);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));

        List<DetectedChange> secondAbsence = service.detect(pair, new ExtractionResult(List.of(), 0));

        assertThat(secondAbsence).hasSize(1);
        assertThat(secondAbsence.get(0).type()).isEqualTo(NotificationType.REMOVED);
        assertThat(existing.getStatus()).isEqualTo(EventStatus.REMOVED);
        assertThat(existing.getMissCount()).isEqualTo(2);

        List<DetectedChange> thirdAbsence = service.detect(pair, new ExtractionResult(List.of(), 0));

        assertThat(thirdAbsence).isEmpty();
        assertThat(existing.getMissCount()).isEqualTo(2);
        assertThat(existing.getStatus()).isEqualTo(EventStatus.REMOVED);
    }

    @Test
    void absentItemInPartialRun_isLeftUntouched() {
        Event existing = activeEvent("ext1", "Title", "Venue", "5 Jul", 0);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(), 1));

        assertThat(changes).isEmpty();
        assertThat(existing.getMissCount()).isZero();
        assertThat(existing.getStatus()).isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    void reappearanceOfRemovedEvent_becomesActiveWithMissCountZeroAndNew() {
        Event existing = Event.builder()
                .id(100L)
                .searchProvider(pair)
                .externalId("ext1")
                .title("Old Title")
                .venue("Old Venue")
                .eventDateRaw("5 Jul")
                .eventDate(SpanishDateParser.parse("5 Jul").orElse(null))
                .rawJson("{}")
                .status(EventStatus.REMOVED)
                .missCount(2)
                .firstSeenAt(Instant.now().minusSeconds(7200))
                .lastSeenAt(Instant.now().minusSeconds(3600))
                .build();
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));
        RawEventData item = new RawEventData("ext1", "New Title", "Old Venue", "Bogota", "5 Jul", "{}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).type()).isEqualTo(NotificationType.NEW);
        assertThat(existing.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(existing.getMissCount()).isZero();
        assertThat(existing.getTitle()).isEqualTo("New Title");

        ArgumentCaptor<List<EventChange>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventChangeRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getFieldName()).isEqualTo("title");
    }

    @Test
    void duplicateExternalIdInSameExtraction_persistsOnlyOneNewEvent() {
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of());
        RawEventData first = new RawEventData("ext1", "Rock Fest 2026", "Movistar Arena", "Bogota", "5 Jul", "{\"a\":1}");
        RawEventData duplicate = new RawEventData("ext1", "Rock Fest 2026", "Movistar Arena", "Bogota", "5 Jul", "{\"a\":2}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(first, duplicate), 0));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).type()).isEqualTo(NotificationType.NEW);
        verify(eventRepository, times(1)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void knownItemWhoseTitleNoLongerContainsTerm_isProcessedAsChangedNotDiscarded() {
        Event existing = activeEvent("ext1", "Rock Fest 2026", "Movistar Arena", "5 Jul", 0);
        when(eventRepository.findBySearchProviderId(10L)).thenReturn(List.of(existing));
        RawEventData item = new RawEventData("ext1", "Random Other Event", "Movistar Arena", "Bogota", "5 Jul", "{}");

        List<DetectedChange> changes = service.detect(pair, new ExtractionResult(List.of(item), 0));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).type()).isEqualTo(NotificationType.CHANGED);
        assertThat(existing.getTitle()).isEqualTo("Random Other Event");
    }
}
