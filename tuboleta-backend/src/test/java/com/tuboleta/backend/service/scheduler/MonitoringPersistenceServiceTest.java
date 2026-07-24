package com.tuboleta.backend.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.ProviderRun;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.repository.ProviderRunRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.service.ChangeDetectionService;
import com.tuboleta.backend.service.NotificationService;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.RawEventData;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests directos del fix de Task 6 para la parte TRANSACCIONAL del
 * monitoreo (REQ-DET-005), separada de {@link MonitoringRunService} para no
 * caer en la trampa de self-invocation de Spring AOP.
 */
@ExtendWith(MockitoExtension.class)
class MonitoringPersistenceServiceTest {

    @Mock
    private ProviderRunRepository providerRunRepository;

    @Mock
    private SearchProviderRepository searchProviderRepository;

    @Mock
    private ChangeDetectionService changeDetectionService;

    @Mock
    private NotificationService notificationService;

    private MonitoringPersistenceService service;

    private Provider provider;
    private SearchProvider pair1;
    private SearchProvider pair2;

    @BeforeEach
    void setUp() {
        provider = Provider.builder().id(1L).name("TuBoleta").build();
        Search search1 = Search.builder().id(1L).termNormalized("rock fest").build();
        Search search2 = Search.builder().id(2L).termNormalized("rock fest").build();
        pair1 = SearchProvider.builder().id(10L).provider(provider).search(search1).build();
        pair2 = SearchProvider.builder().id(11L).provider(provider).search(search2).build();
        service = new MonitoringPersistenceService(providerRunRepository, searchProviderRepository,
                changeDetectionService, notificationService);
    }

    @Test
    void recordRun_savesOneProviderRunWithCorrectFieldsAndTouchesLastRunAtOnAllPairs() {
        Instant startedAt = Instant.now().minusSeconds(5);
        List<SearchProvider> pairs = List.of(pair1, pair2);

        service.recordRun(provider, "rock fest", true, null, 3, 2, startedAt, pairs);

        ArgumentCaptor<ProviderRun> runCaptor = ArgumentCaptor.forClass(ProviderRun.class);
        verify(providerRunRepository).save(runCaptor.capture());
        ProviderRun run = runCaptor.getValue();
        assertThat(run.getProvider()).isEqualTo(provider);
        assertThat(run.getTermNormalized()).isEqualTo("rock fest");
        assertThat(run.getStartedAt()).isEqualTo(startedAt);
        assertThat(run.getFinishedAt()).isNotNull();
        assertThat(run.getSuccess()).isTrue();
        assertThat(run.getErrorMessage()).isNull();
        assertThat(run.getEventsFound()).isEqualTo(3);
        assertThat(run.getPairsApplied()).isEqualTo(2);

        verify(searchProviderRepository).saveAll(eq(pairs));
        assertThat(pair1.getLastRunAt()).isEqualTo(startedAt);
        assertThat(pair2.getLastRunAt()).isEqualTo(startedAt);
    }

    @Test
    void recordRun_failureCase_savesProviderRunWithErrorMessageAndStillTouchesLastRunAt() {
        Instant startedAt = Instant.now();
        List<SearchProvider> pairs = List.of(pair1);

        service.recordRun(provider, "rock fest", false, "boom", null, 0, startedAt, pairs);

        ArgumentCaptor<ProviderRun> runCaptor = ArgumentCaptor.forClass(ProviderRun.class);
        verify(providerRunRepository).save(runCaptor.capture());
        ProviderRun run = runCaptor.getValue();
        assertThat(run.getSuccess()).isFalse();
        assertThat(run.getErrorMessage()).isEqualTo("boom");
        assertThat(run.getEventsFound()).isNull();
        assertThat(run.getPairsApplied()).isZero();

        verify(searchProviderRepository).saveAll(eq(pairs));
        assertThat(pair1.getLastRunAt()).isEqualTo(startedAt);
    }

    @Test
    void applyPair_callsDetectThenPassesResultToNotify() {
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 0);
        List<DetectedChange> changes = List.of();
        when(changeDetectionService.detect(pair1, extraction)).thenReturn(changes);

        service.applyPair(pair1, extraction);

        verify(changeDetectionService).detect(pair1, extraction);
        verify(notificationService).notifyDetectedChanges(eq(pair1), eq(changes));
    }

    @Test
    void applyPair_propagatesExceptionFromNotify() {
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 0);
        when(changeDetectionService.detect(pair1, extraction)).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new RuntimeException("envio fallido"))
                .when(notificationService).notifyDetectedChanges(eq(pair1), anyList());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> service.applyPair(pair1, extraction));

        verify(changeDetectionService).detect(pair1, extraction);
    }
}
