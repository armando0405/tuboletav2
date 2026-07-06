package com.tuboleta.backend.service.scheduler;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.ProviderRun;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.repository.ProviderRunRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.service.ChangeDetectionService;
import com.tuboleta.backend.service.NotificationService;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parte TRANSACCIONAL del monitoreo (REQ-DET-005), separada de
 * {@link MonitoringRunService} para evitar la trampa de self-invocation de
 * Spring AOP: un método {@code @Transactional} invocado desde OTRO método
 * del MISMO bean no pasa por el proxy, así que la transacción se ignora. Al
 * vivir en un bean aparte, {@link MonitoringRunService} sí pasa por el proxy
 * cuando llama a estos métodos.
 *
 * <p>La extracción HTTP (potencialmente lenta) NUNCA ocurre dentro de estas
 * transacciones — solo la persistencia, para no retener conexiones JDBC del
 * pool durante segundos.</p>
 */
@Service
public class MonitoringPersistenceService {

    private final ProviderRunRepository providerRunRepository;
    private final SearchProviderRepository searchProviderRepository;
    private final ChangeDetectionService changeDetectionService;
    private final NotificationService notificationService;

    public MonitoringPersistenceService(ProviderRunRepository providerRunRepository,
                                         SearchProviderRepository searchProviderRepository,
                                         ChangeDetectionService changeDetectionService,
                                         NotificationService notificationService) {
        this.providerRunRepository = providerRunRepository;
        this.searchProviderRepository = searchProviderRepository;
        this.changeDetectionService = changeDetectionService;
        this.notificationService = notificationService;
    }

    /**
     * Detección + notificación ATÓMICAS para UN par búsqueda↔proveedor: si
     * la notificación falla, el detect de ESE par hace rollback (el próximo
     * tick vuelve a detectar/notificar sobre el mismo resultado de
     * extracción ya persistido en memoria del run actual). Propaga la
     * excepción — es responsabilidad del orquestador ({@link
     * MonitoringRunService}) decidir cómo seguir con los demás pares del
     * grupo.
     *
     * @param pair       par búsqueda↔proveedor sobre el que aplicar el resultado
     * @param extraction resultado de la extracción ya realizada (fuera de esta transacción)
     */
    @Transactional
    public void applyPair(SearchProvider pair, ExtractionResult extraction) {
        List<DetectedChange> changes = changeDetectionService.detect(pair, extraction);
        notificationService.notifyDetectedChanges(pair, changes);
    }

    /**
     * Registra el resultado de la corrida ({@code ProviderRun}) y actualiza
     * {@code last_run_at} en TODOS los pares del grupo. Se invoca SIEMPRE
     * (éxito o fallo, REQ-DET-005 punto 6): un proveedor caído o un par que
     * falló no debe reintentarse en cada tick, sino esperar a su siguiente
     * ventana de frecuencia.
     *
     * @param provider       proveedor de la corrida
     * @param termNormalized término normalizado común al grupo
     * @param success        si la corrida fue completamente exitosa
     * @param errorMessage   detalle del fallo, o {@code null} en éxito
     * @param eventsFound    ítems extraídos, o {@code null} si no hubo extracción
     * @param pairsApplied   nº real de pares donde detección+notificación se aplicaron sin error
     * @param startedAt      instante de inicio de la corrida (también usado como {@code last_run_at})
     * @param pairs          todos los pares del grupo (aplicados o no)
     */
    @Transactional
    public void recordRun(Provider provider, String termNormalized, boolean success, String errorMessage,
                           Integer eventsFound, int pairsApplied, Instant startedAt, List<SearchProvider> pairs) {
        ProviderRun run = ProviderRun.builder()
                .provider(provider)
                .termNormalized(termNormalized)
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .success(success)
                .errorMessage(errorMessage)
                .eventsFound(eventsFound)
                .pairsApplied(pairsApplied)
                .build();
        providerRunRepository.save(run);

        for (SearchProvider pair : pairs) {
            pair.setLastRunAt(startedAt);
        }
        searchProviderRepository.saveAll(pairs);
    }
}
