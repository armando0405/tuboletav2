package com.tuboleta.backend.service.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Único dispatcher del monitoreo automático (REQ-ARQ-004: solo orquestación,
 * la lógica vive en {@link MonitoringRunService}). Cada tick pide el trabajo
 * vencido (REQ-DET-005), lo agrupa POR PROVEEDOR y envía un job por
 * proveedor al pool — paralelismo ENTRE proveedores. Dentro de un proveedor
 * los grupos se procesan secuencialmente, con una pausa
 * {@code rate_limit_ms} (config del proveedor) ENTRE peticiones (nunca antes
 * de la primera).
 *
 * <p>Si un tick anterior sigue procesando un proveedor, el tick actual no
 * duplica su trabajo: se lleva un registro simple de proveedores en curso
 * ({@link ConcurrentHashMap}-backed set) — sin lock distribuido, ya que el
 * backend corre en una sola instancia (REQ-DET-005 punto 8).</p>
 */
@Component
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class MonitoringDispatcher {

    private static final Logger log = LogManager.getLogger(MonitoringDispatcher.class);
    private static final long DEFAULT_RATE_LIMIT_MS = 2000L;

    /**
     * Instancia propia y no gestionada por Spring (mismo motivo que
     * {@code TuBoletaScraperExtractor}: en Spring Boot 4 el
     * {@code ObjectMapper} auto-configurado es Jackson 3, no
     * {@code com.fasterxml.jackson}).
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DueWorkSelector dueWorkSelector;
    private final MonitoringRunService monitoringRunService;
    private final Executor monitoringTaskExecutor;
    private final Sleeper sleeper;

    private final Set<Long> providersInProgress = ConcurrentHashMap.newKeySet();

    public MonitoringDispatcher(DueWorkSelector dueWorkSelector,
                                 MonitoringRunService monitoringRunService,
                                 @Qualifier("monitoringTaskExecutor") Executor monitoringTaskExecutor,
                                 Sleeper sleeper) {
        this.dueWorkSelector = dueWorkSelector;
        this.monitoringRunService = monitoringRunService;
        this.monitoringTaskExecutor = monitoringTaskExecutor;
        this.sleeper = sleeper;
    }

    @Scheduled(fixedDelayString = "${scheduler.tick-ms:60000}")
    public void tick() {
        try {
            List<DueGroup> dueWork = dueWorkSelector.selectDueWork(Instant.now());
            if (dueWork.isEmpty()) {
                log.debug("Tick del scheduler: sin trabajo vencido");
                return;
            }

            Map<Long, List<DueGroup>> byProvider = dueWork.stream()
                    .collect(Collectors.groupingBy(g -> g.provider().getId(), LinkedHashMap::new, Collectors.toList()));

            log.info("Tick del scheduler: {} grupo(s) vencido(s) en {} proveedor(es)",
                    dueWork.size(), byProvider.size());

            for (Map.Entry<Long, List<DueGroup>> entry : byProvider.entrySet()) {
                Long providerId = entry.getKey();
                if (!providersInProgress.add(providerId)) {
                    log.info("Proveedor id={} ya tiene un job en curso, se omite este tick para no duplicar", providerId);
                    continue;
                }
                List<DueGroup> groups = entry.getValue();
                log.info("Despachando proveedor id={} con {} grupo(s) al pool de monitoreo", providerId, groups.size());
                monitoringTaskExecutor.execute(() -> {
                    try {
                        processProviderGroups(groups);
                    } finally {
                        providersInProgress.remove(providerId);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error en el tick del scheduler, se reintentará en el próximo ciclo", e);
        }
    }

    /**
     * Procesa SECUENCIALMENTE los grupos de un mismo proveedor, con la pausa
     * de rate-limit entre ellos (nunca antes del primero). Una excepción en
     * un grupo no interrumpe los demás.
     */
    void processProviderGroups(List<DueGroup> groups) {
        if (groups.isEmpty()) {
            return;
        }
        long rateLimitMs = rateLimitMs(groups.get(0).provider());
        for (int i = 0; i < groups.size(); i++) {
            if (i > 0) {
                try {
                    sleeper.sleep(rateLimitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Pausa de rate-limit interrumpida, se detiene el resto de grupos de este proveedor");
                    return;
                }
            }
            DueGroup group = groups.get(i);
            try {
                monitoringRunService.runGroup(group);
            } catch (Exception e) {
                log.error("Error inesperado procesando el grupo proveedor='{}' término='{}', se continúa con los demás",
                        group.provider().getName(), group.termNormalized(), e);
            }
        }
    }

    private long rateLimitMs(Provider provider) {
        JsonNode configNode = parseConfig(provider.getConfig());
        return configNode.path("rate_limit_ms").asLong(DEFAULT_RATE_LIMIT_MS);
    }

    private JsonNode parseConfig(String config) {
        if (config == null || config.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(config);
        } catch (IOException e) {
            log.warn("Config JSONB inválido para rate_limit_ms, se usa el valor por defecto", e);
            return objectMapper.createObjectNode();
        }
    }
}
