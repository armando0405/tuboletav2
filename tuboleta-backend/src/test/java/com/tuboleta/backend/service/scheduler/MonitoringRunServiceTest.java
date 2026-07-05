package com.tuboleta.backend.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.ProviderExtractor;
import com.tuboleta.backend.service.extraction.RawEventData;
import com.tuboleta.backend.utils.exception.ScrapingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos mínimos del brief de Task 6 para la ejecución de un grupo
 * (REQ-DET-005): 1 extracción por grupo, N diffs/notificaciones por par,
 * registro en {@code provider_runs} y {@code last_run_at} siempre actualizado.
 */
@ExtendWith(MockitoExtension.class)
class MonitoringRunServiceTest {

    @Mock
    private ProviderRunRepository providerRunRepository;

    @Mock
    private ChangeDetectionService changeDetectionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SearchProviderRepository searchProviderRepository;

    @Mock
    private ProviderExtractor extractor;

    private MonitoringRunService service;

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
        service = new MonitoringRunService(providerRunRepository, List.of(extractor),
                changeDetectionService, notificationService, searchProviderRepository);
    }

    @Test
    void groupOfTwoPairs_oneExtractTwoDetectTwoNotify_successTrueAndLastRunAtUpdatedOnBoth() {
        when(extractor.supports(provider)).thenReturn(true);
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 0);
        when(extractor.extract(provider, "rock fest")).thenReturn(extraction);
        when(changeDetectionService.detect(any(), eq(extraction))).thenReturn(List.of());

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1, pair2));
        service.runGroup(group);

        verify(extractor, times(1)).extract(provider, "rock fest");
        verify(changeDetectionService, times(1)).detect(eq(pair1), eq(extraction));
        verify(changeDetectionService, times(1)).detect(eq(pair2), eq(extraction));
        verify(notificationService, times(2)).notifyDetectedChanges(any(), anyList());

        ArgumentCaptor<ProviderRun> captor = ArgumentCaptor.forClass(ProviderRun.class);
        verify(providerRunRepository).save(captor.capture());
        ProviderRun run = captor.getValue();
        assertThat(run.getSuccess()).isTrue();
        assertThat(run.getPairsApplied()).isEqualTo(2);
        assertThat(run.getEventsFound()).isEqualTo(1);
        assertThat(pair1.getLastRunAt()).isNotNull();
        assertThat(pair2.getLastRunAt()).isNotNull();
    }

    @Test
    void extractorThrowsScrapingException_runFailsButLastRunAtStillUpdated_noDetectNoNotify() {
        when(extractor.supports(provider)).thenReturn(true);
        when(extractor.extract(provider, "rock fest")).thenThrow(new ScrapingException("boom", null));

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1));
        service.runGroup(group);

        verify(changeDetectionService, never()).detect(any(), any());
        verify(notificationService, never()).notifyDetectedChanges(any(), anyList());

        ArgumentCaptor<ProviderRun> captor = ArgumentCaptor.forClass(ProviderRun.class);
        verify(providerRunRepository).save(captor.capture());
        ProviderRun run = captor.getValue();
        assertThat(run.getSuccess()).isFalse();
        assertThat(run.getErrorMessage()).isNotBlank();
        assertThat(pair1.getLastRunAt()).isNotNull();
    }

    @Test
    void extractionWithFailedItems_detectAndNotifyStillRun_butSuccessFalse() {
        when(extractor.supports(provider)).thenReturn(true);
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 1);
        when(extractor.extract(provider, "rock fest")).thenReturn(extraction);
        when(changeDetectionService.detect(any(), eq(extraction))).thenReturn(List.of());

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1));
        service.runGroup(group);

        verify(changeDetectionService, times(1)).detect(eq(pair1), eq(extraction));
        verify(notificationService, times(1)).notifyDetectedChanges(any(), anyList());

        ArgumentCaptor<ProviderRun> captor = ArgumentCaptor.forClass(ProviderRun.class);
        verify(providerRunRepository).save(captor.capture());
        assertThat(captor.getValue().getSuccess()).isFalse();
        assertThat(pair1.getLastRunAt()).isNotNull();
    }

    @Test
    void noExtractorSupportsProvider_successFalse() {
        when(extractor.supports(provider)).thenReturn(false);

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1));
        service.runGroup(group);

        verify(changeDetectionService, never()).detect(any(), any());
        verify(notificationService, never()).notifyDetectedChanges(any(), anyList());

        ArgumentCaptor<ProviderRun> captor = ArgumentCaptor.forClass(ProviderRun.class);
        verify(providerRunRepository).save(captor.capture());
        assertThat(captor.getValue().getSuccess()).isFalse();
        assertThat(captor.getValue().getErrorMessage()).contains("TuBoleta");
        assertThat(pair1.getLastRunAt()).isNotNull();
    }
}
