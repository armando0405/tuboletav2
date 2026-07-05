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
import com.tuboleta.backend.service.extraction.ProviderExtractor;
import java.time.Instant;
import java.text.MessageFormat;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ejecuta UN grupo de trabajo vencido (REQ-DET-005): una sola extracción por
 * {@code (provider, term_normalized)}, aplicada a cada par suscrito
 * (extracción→detección→notificación), y deja registro en
 * {@code provider_runs}. Servicio aparte del dispatcher para que la
 * transaccionalidad cubra exactamente un grupo — el dispatcher solo orquesta
 * (REQ-ARQ-004).
 *
 * <p>{@code last_run_at} se actualiza SIEMPRE (éxito o fallo, REQ-DET-005 punto 6):
 * un proveedor caído no se reintenta cada tick, espera a su siguiente ventana
 * de frecuencia.</p>
 */
@Service
public class MonitoringRunService {

    private static final Logger log = LogManager.getLogger(MonitoringRunService.class);

    private final ProviderRunRepository providerRunRepository;
    private final List<ProviderExtractor> extractors;
    private final ChangeDetectionService changeDetectionService;
    private final NotificationService notificationService;
    private final SearchProviderRepository searchProviderRepository;

    public MonitoringRunService(ProviderRunRepository providerRunRepository,
                                 List<ProviderExtractor> extractors,
                                 ChangeDetectionService changeDetectionService,
                                 NotificationService notificationService,
                                 SearchProviderRepository searchProviderRepository) {
        this.providerRunRepository = providerRunRepository;
        this.extractors = extractors;
        this.changeDetectionService = changeDetectionService;
        this.notificationService = notificationService;
        this.searchProviderRepository = searchProviderRepository;
    }

    /**
     * Corre un grupo completo: crea el {@code ProviderRun}, extrae, aplica
     * detección + notificación por cada par, completa el run y actualiza
     * {@code last_run_at} en todos los pares del grupo, pase lo que pase.
     *
     * @param group grupo de trabajo vencido (mismo provider + término)
     */
    @Transactional
    public void runGroup(DueGroup group) {
        Provider provider = group.provider();
        String termNormalized = group.termNormalized();
        Instant startedAt = Instant.now();
        ProviderRun run = ProviderRun.builder()
                .provider(provider)
                .termNormalized(termNormalized)
                .startedAt(startedAt)
                .build();

        ProviderExtractor extractor = findExtractor(provider);
        if (extractor == null) {
            String message = MessageFormat.format("No hay extractor para el proveedor {0}", provider.getName());
            log.error(message);
            finishRun(run, false, message, null, group.pairs().size());
            touchLastRunAt(group, startedAt);
            return;
        }

        ExtractionResult extraction;
        try {
            extraction = extractor.extract(provider, termNormalized);
        } catch (Exception e) {
            log.error("Error al extraer proveedor '{}' término '{}'", provider.getName(), termNormalized, e);
            finishRun(run, false, e.getMessage(), null, group.pairs().size());
            touchLastRunAt(group, startedAt);
            return;
        }

        for (SearchProvider pair : group.pairs()) {
            List<DetectedChange> changes = changeDetectionService.detect(pair, extraction);
            notificationService.notifyDetectedChanges(pair, changes);
        }

        boolean success = extraction.failedItems() == 0;
        finishRun(run, success, null, extraction.items().size(), group.pairs().size());
        touchLastRunAt(group, startedAt);
    }

    private ProviderExtractor findExtractor(Provider provider) {
        return extractors.stream()
                .filter(e -> e.supports(provider))
                .findFirst()
                .orElse(null);
    }

    private void finishRun(ProviderRun run, boolean success, String errorMessage,
                            Integer eventsFound, int pairsApplied) {
        run.setFinishedAt(Instant.now());
        run.setSuccess(success);
        run.setErrorMessage(errorMessage);
        run.setEventsFound(eventsFound);
        run.setPairsApplied(pairsApplied);
        providerRunRepository.save(run);
    }

    private void touchLastRunAt(DueGroup group, Instant startedAt) {
        for (SearchProvider pair : group.pairs()) {
            pair.setLastRunAt(startedAt);
        }
        searchProviderRepository.saveAll(group.pairs());
    }
}
