package com.tuboleta.backend.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.ProviderExtractor;
import com.tuboleta.backend.service.extraction.RawEventData;
import com.tuboleta.backend.utils.exception.ScrapingException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos del fix de Task 6 para la ejecución de un grupo (REQ-DET-005):
 * {@code runGroup} ahora es un orquestador NO transaccional que delega toda
 * la persistencia (detección+notificación por par y el registro de la
 * corrida) a {@link MonitoringPersistenceService} — mockeado aquí para
 * aislar la lógica de orquestación (extracción fuera de transacción,
 * aislamiento entre pares, cálculo de {@code pairsApplied}/{@code errorMessage}).
 */
@ExtendWith(MockitoExtension.class)
class MonitoringRunServiceTest {

    @Mock
    private MonitoringPersistenceService persistence;

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
        service = new MonitoringRunService(List.of(extractor), persistence);
    }

    @Test
    void groupOfTwoPairs_extractionOk_applyPairCalledTwiceAndRecordRunSuccess() {
        when(extractor.supports(provider)).thenReturn(true);
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 0);
        when(extractor.extract(provider, "rock fest")).thenReturn(extraction);

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1, pair2));
        service.runGroup(group);

        verify(extractor, times(1)).extract(provider, "rock fest");
        verify(persistence, times(1)).applyPair(pair1, extraction);
        verify(persistence, times(1)).applyPair(pair2, extraction);
        verify(persistence).recordRun(eq(provider), eq("rock fest"), eq(true), isNull(),
                eq(1), eq(2), any(Instant.class), eq(List.of(pair1, pair2)));
    }

    @Test
    void extractorThrowsScrapingException_applyPairNeverCalled_recordRunFailureNoEventsFound() {
        when(extractor.supports(provider)).thenReturn(true);
        when(extractor.extract(provider, "rock fest")).thenThrow(new ScrapingException("boom", null));

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1));
        service.runGroup(group);

        verify(persistence, never()).applyPair(any(), any());
        verify(persistence).recordRun(eq(provider), eq("rock fest"), eq(false), eq("boom"),
                isNull(), eq(0), any(Instant.class), eq(List.of(pair1)));
    }

    @Test
    void extractionWithFailedItems_applyPairCalledForBothPairs_recordRunFailureWithMessage() {
        when(extractor.supports(provider)).thenReturn(true);
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 1);
        when(extractor.extract(provider, "rock fest")).thenReturn(extraction);

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1, pair2));
        service.runGroup(group);

        verify(persistence, times(1)).applyPair(pair1, extraction);
        verify(persistence, times(1)).applyPair(pair2, extraction);

        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        verify(persistence).recordRun(eq(provider), eq("rock fest"), eq(false), errorCaptor.capture(),
                eq(1), eq(2), any(Instant.class), eq(List.of(pair1, pair2)));
        assertThat(errorCaptor.getValue()).contains("fallaron");
    }

    @Test
    void noExtractorSupportsProvider_applyPairNeverCalled_recordRunFailureNoExtraction() {
        when(extractor.supports(provider)).thenReturn(false);

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1));
        service.runGroup(group);

        verify(persistence, never()).applyPair(any(), any());

        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        verify(persistence).recordRun(eq(provider), eq("rock fest"), eq(false), errorCaptor.capture(),
                isNull(), eq(0), any(Instant.class), eq(List.of(pair1)));
        assertThat(errorCaptor.getValue()).contains("TuBoleta");
    }

    @Test
    void groupOfTwoPairs_firstApplyPairThrows_secondPairStillApplied_recordRunFailurePairsAppliedOne() {
        when(extractor.supports(provider)).thenReturn(true);
        RawEventData item = new RawEventData("ext1", "Rock Fest", "Arena", "Bogota", "5 Jul", "{}");
        ExtractionResult extraction = new ExtractionResult(List.of(item), 0);
        when(extractor.extract(provider, "rock fest")).thenReturn(extraction);
        doThrow(new RuntimeException("fallo notificando")).when(persistence).applyPair(pair1, extraction);

        DueGroup group = new DueGroup(provider, "rock fest", List.of(pair1, pair2));
        service.runGroup(group);

        verify(persistence, times(1)).applyPair(pair1, extraction);
        verify(persistence, times(1)).applyPair(pair2, extraction);
        verify(persistence).recordRun(eq(provider), eq("rock fest"), eq(false), any(String.class),
                eq(1), eq(1), any(Instant.class), eq(List.of(pair1, pair2)));
    }
}
