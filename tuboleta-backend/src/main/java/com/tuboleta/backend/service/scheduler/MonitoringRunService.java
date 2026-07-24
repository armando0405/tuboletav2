package com.tuboleta.backend.service.scheduler;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.ProviderExtractor;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Orquesta UN grupo de trabajo vencido (REQ-DET-005): una sola extracción por
 * {@code (provider, term_normalized)}, aplicada a cada par suscrito
 * (extracción→detección→notificación), y deja registro en
 * {@code provider_runs}. Servicio aparte del dispatcher para que quede claro
 * qué corresponde a un grupo — el dispatcher solo orquesta (REQ-ARQ-004).
 *
 * <p>Este servicio NO es transaccional a propósito: la extracción es una
 * llamada HTTP (potencialmente varios segundos) y jamás debe retener una
 * conexión JDBC del pool mientras espera al proveedor externo — con un pool
 * pequeño eso agota las conexiones disponibles. Lo transaccional (persistir
 * el {@code ProviderRun} y aplicar detección+notificación por par) vive en
 * {@link MonitoringPersistenceService}, un bean aparte para no caer en la
 * trampa de self-invocation de Spring AOP (un {@code @Transactional}
 * invocado desde otro método del MISMO bean no pasa por el proxy).</p>
 *
 * <p>{@code last_run_at} y el {@code ProviderRun} se registran SIEMPRE
 * (éxito o fallo, REQ-DET-005 punto 6): un proveedor caído no se reintenta
 * cada tick, espera a su siguiente ventana de frecuencia. El fallo de
 * detección/notificación de UN par tampoco detiene a los demás pares del
 * grupo ni impide que se registre la corrida.</p>
 */
@Service
public class MonitoringRunService {

    private static final Logger log = LogManager.getLogger(MonitoringRunService.class);

    private final List<ProviderExtractor> extractors;
    private final MonitoringPersistenceService persistence;

    public MonitoringRunService(List<ProviderExtractor> extractors,
                                 MonitoringPersistenceService persistence) {
        this.extractors = extractors;
        this.persistence = persistence;
    }

    /**
     * Corre un grupo completo: extrae (fuera de transacción), aplica
     * detección + notificación por cada par de forma aislada (el fallo de
     * uno no detiene a los demás) y registra el resultado de la corrida,
     * pase lo que pase.
     *
     * @param group grupo de trabajo vencido (mismo provider + término)
     */
    public void runGroup(DueGroup group) {
        Instant startedAt = Instant.now();
        Provider provider = group.provider();
        String term = group.termNormalized();

        log.info("Corrida iniciada: proveedor='{}' término='{}' ({} par(es) suscrito(s))",
                provider.getName(), term, group.pairs().size());

        ProviderExtractor extractor = findExtractor(provider);
        if (extractor == null) {
            String msg = MessageFormat.format("No hay extractor para el proveedor {0}", provider.getName());
            log.error(msg);
            persistence.recordRun(provider, term, false, msg, null, 0, startedAt, group.pairs());
            return;
        }

        ExtractionResult extraction;
        try {
            extraction = extractor.extract(provider, term);
        } catch (Exception e) {
            log.error("Error al extraer proveedor '{}' término '{}'", provider.getName(), term, e);
            persistence.recordRun(provider, term, false, e.getMessage(), null, 0, startedAt, group.pairs());
            return;
        }

        log.info("Extracción OK: proveedor='{}' término='{}' -> {} ítem(es) extraído(s), {} fallido(s)",
                provider.getName(), term, extraction.items().size(), extraction.failedItems());

        int pairsApplied = 0;
        boolean anyPairFailed = false;
        for (SearchProvider pair : group.pairs()) {
            try {
                persistence.applyPair(pair, extraction);
                pairsApplied++;
            } catch (Exception e) {
                anyPairFailed = true;
                log.error("Error aplicando detección/notificación al par id={} (búsqueda '{}'), se continúa con los demás",
                        pair.getId(), term, e);
            }
        }

        boolean success = extraction.failedItems() == 0 && !anyPairFailed;
        String errorMessage = null;
        if (!success) {
            if (extraction.failedItems() > 0 && anyPairFailed) {
                errorMessage = MessageFormat.format(
                        "{0} items fallaron al extraerse y al menos un par fallo al aplicarse", extraction.failedItems());
            } else if (extraction.failedItems() > 0) {
                errorMessage = MessageFormat.format("{0} items fallaron al extraerse", extraction.failedItems());
            } else {
                errorMessage = "Al menos un par fallo al aplicar deteccion/notificacion";
            }
        }
        persistence.recordRun(provider, term, success, errorMessage, extraction.items().size(), pairsApplied, startedAt,
                group.pairs());
        log.info("Corrida finalizada: proveedor='{}' término='{}' success={} pares_aplicados={}/{}{}",
                provider.getName(), term, success, pairsApplied, group.pairs().size(),
                errorMessage == null ? "" : " (" + errorMessage + ")");
    }

    private ProviderExtractor findExtractor(Provider provider) {
        return extractors.stream()
                .filter(e -> e.supports(provider))
                .findFirst()
                .orElse(null);
    }
}
