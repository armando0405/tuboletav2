package com.tuboleta.backend.service.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tuboleta.backend.domain.entities.Provider;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Caso mínimo del brief de Task 6 para el rate-limit dentro de un mismo
 * proveedor (REQ-DET-005): la pausa configurable ocurre ENTRE grupos, nunca
 * antes del primero. Usa un executor "mismo hilo" para no depender de hilos
 * reales, y un {@link Sleeper} mockeado para no dormir de verdad.
 */
@ExtendWith(MockitoExtension.class)
class MonitoringDispatcherTest {

    @Mock
    private DueWorkSelector dueWorkSelector;

    @Mock
    private MonitoringRunService monitoringRunService;

    @Mock
    private Sleeper sleeper;

    private MonitoringDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        Executor sameThreadExecutor = Runnable::run;
        dispatcher = new MonitoringDispatcher(dueWorkSelector, monitoringRunService, sameThreadExecutor, sleeper);
    }

    @Test
    void twoGroupsSameProvider_sleepsExactlyOnceBetweenThem() throws InterruptedException {
        Provider provider = Provider.builder().id(1L).name("TuBoleta").config("{\"rate_limit_ms\":1500}").build();
        DueGroup group1 = new DueGroup(provider, "rock fest", List.of());
        DueGroup group2 = new DueGroup(provider, "jazz fest", List.of());

        dispatcher.processProviderGroups(List.of(group1, group2));

        verify(sleeper, times(1)).sleep(1500L);
        verify(monitoringRunService, times(1)).runGroup(group1);
        verify(monitoringRunService, times(1)).runGroup(group2);
    }
}
