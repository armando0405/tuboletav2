package com.tuboleta.backend.service.notification;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * Envío transaccional vía Mailgun (misma mecánica que {@link MailgunEmailSender}
 * del flujo de notificaciones). Activo solo si hay {@code mailgun.api-key}.
 */
@Service
@ConditionalOnExpression("!'${notifications.mailgun.api-key:}'.isEmpty()")
public class MailgunEmailService implements EmailService {

    private static final Logger log = LogManager.getLogger(MailgunEmailService.class);

    private final String apiKey;
    private final String senderUrl;
    private final String from;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public MailgunEmailService(
            @Value("${notifications.mailgun.api-key}") String apiKey,
            @Value("${notifications.mailgun.sender-url}") String senderUrl,
            @Value("${notifications.mailgun.from}") String from) {
        this.apiKey = apiKey;
        this.senderUrl = senderUrl;
        this.from = from;
    }

    @Override
    public boolean sendHtml(String to, String subject, String htmlBody) {
        try {
            String body = "from=" + enc(from)
                    + "&to=" + enc(to)
                    + "&subject=" + enc(subject)
                    + "&html=" + enc(htmlBody);
            String auth = Base64.getEncoder()
                    .encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(senderUrl))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            log.info("Enviando correo transaccional a '{}' (asunto: \"{}\") vía Mailgun", to, subject);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
            if (ok) {
                log.info("Correo transaccional enviado a '{}' — Mailgun respondió estado {}", to, response.statusCode());
            } else {
                log.warn("Mailgun respondió {} al enviar a '{}'", response.statusCode(), to);
            }
            return ok;
        } catch (IOException e) {
            log.error("Error de E/S al enviar correo transaccional a '{}'", to, e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Envío transaccional interrumpido al enviar a '{}'", to, e);
            return false;
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
