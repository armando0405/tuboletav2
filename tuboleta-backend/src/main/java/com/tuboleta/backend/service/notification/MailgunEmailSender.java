package com.tuboleta.backend.service.notification;

import com.tuboleta.backend.domain.entities.Notification;
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
import org.springframework.stereotype.Component;

/**
 * Envío EMAIL vía la API de Mailgun (REQ-NOT-002/005). Se activa solo si
 * {@code notifications.mailgun.api-key} llegó no vacío. Tiene PRIORIDAD sobre
 * {@link SendGridEmailSender}: si hay key de Mailgun, este es el sender de
 * EMAIL; si no, se usa SendGrid (si tiene key) o el {@link LoggingEmailSender}.
 * Exactamente uno de los tres se activa (condiciones mutuamente excluyentes),
 * porque solo puede haber un {@link ChannelSender} por canal.
 *
 * <p>Contrato de Mailgun: POST {@code form-urlencoded} a la sender-url con
 * {@code from/to/subject/html}, autenticando con Basic Auth
 * {@code base64("api:" + apiKey)} (misma mecánica que el proyecto de
 * referencia en {@code servicio-email.md}). Sin dependencias externas: usa el
 * {@link HttpClient} nativo de Java.</p>
 */
@Component
@ConditionalOnExpression("!'${notifications.mailgun.api-key:}'.isEmpty()")
public class MailgunEmailSender implements ChannelSender {

    private static final Logger log = LogManager.getLogger(MailgunEmailSender.class);

    private final String apiKey;
    private final String senderUrl;
    private final String from;
    private final EmailContentBuilder contentBuilder = new EmailContentBuilder();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public MailgunEmailSender(
            @Value("${notifications.mailgun.api-key}") String apiKey,
            @Value("${notifications.mailgun.sender-url}") String senderUrl,
            @Value("${notifications.mailgun.from}") String from) {
        this.apiKey = apiKey;
        this.senderUrl = senderUrl;
        this.from = from;
    }

    @Override
    public String channelName() {
        return "EMAIL";
    }

    @Override
    public boolean send(Notification notification, String destination) {
        String subject = contentBuilder.subject(notification);
        try {
            String body = "from=" + enc(from)
                    + "&to=" + enc(destination)
                    + "&subject=" + enc(subject)
                    + "&html=" + enc(contentBuilder.htmlBody(notification));
            String auth = Base64.getEncoder()
                    .encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(senderUrl))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            log.info("Enviando email a '{}' (asunto: \"{}\") vía Mailgun desde '{}'", destination, subject, from);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
            if (ok) {
                log.info("Email enviado a '{}' — Mailgun respondió estado {}", destination, response.statusCode());
            } else {
                log.warn("Mailgun respondió {} al enviar a '{}' — cuerpo: {}",
                        response.statusCode(), destination, truncate(response.body()));
            }
            return ok;
        } catch (IOException e) {
            log.error("Error de E/S al enviar correo por Mailgun a '{}'", destination, e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Envío por Mailgun interrumpido al enviar a '{}'", destination, e);
            return false;
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 300 ? body.substring(0, 300) : body;
    }
}
