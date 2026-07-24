package com.tuboleta.backend.service.scheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Auto-ping para que el plan gratuito de Render no duerma el servicio por
 * inactividad (~15 min sin tráfico entrante). Cada cierto intervalo el propio
 * servicio hace un GET a su URL pública ({@code keepalive.url}); esa petición
 * sale a internet y regresa como tráfico entrante, manteniéndolo despierto.
 *
 * <p>Se activa SOLO si {@code keepalive.url} tiene valor (en Render la variable
 * {@code KEEPALIVE_URL}); en local queda desactivado. Nota honesta: esto lo
 * mantiene despierto mientras ya esté despierto, pero NO puede despertarlo si
 * llegó a dormirse (el scheduler tampoco corre dormido). Para ese caso conviene
 * además un pinger externo (UptimeRobot / cron-job.org).</p>
 */
@Service
@ConditionalOnExpression("!'${keepalive.url:}'.isEmpty()")
public class KeepAliveService {

    private static final Logger log = LogManager.getLogger(KeepAliveService.class);

    private final String healthUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public KeepAliveService(@Value("${keepalive.url}") String url) {
        String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.healthUrl = base + "/actuator/health";
        log.info("Keep-alive activo: se hará ping a {} para evitar el sleep de Render", healthUrl);
    }

    @Scheduled(
            fixedDelayString = "${keepalive.interval-ms:600000}",
            initialDelayString = "${keepalive.interval-ms:600000}")
    public void ping() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<Void> response = http.send(request, HttpResponse.BodyHandlers.discarding());
            log.info("Keep-alive ping -> estado {}", response.statusCode());
        } catch (Exception e) {
            log.warn("Keep-alive ping falló: {}", e.getMessage());
        }
    }
}
