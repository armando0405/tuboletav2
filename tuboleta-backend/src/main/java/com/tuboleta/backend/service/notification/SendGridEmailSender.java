package com.tuboleta.backend.service.notification;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.tuboleta.backend.domain.entities.Notification;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Envío EMAIL real vía SendGrid (REQ-NOT-002/005). Solo se activa si
 * {@code notifications.sendgrid.api-key} llegó no vacío por variable de
 * entorno ({@code SENDGRID_API_KEY}) — nunca hay un valor real versionado.
 *
 * <p>Se usa {@code ConditionalOnExpression} en vez de
 * {@code ConditionalOnProperty} simple porque este último considera
 * "presente" incluso una cadena vacía (el valor por defecto de
 * {@code ${SENDGRID_API_KEY:}} cuando la variable no está seteada), lo que
 * activaría este bean sin credenciales reales.</p>
 */
@Component
@ConditionalOnExpression("!'${notifications.sendgrid.api-key:}'.isEmpty()")
public class SendGridEmailSender implements ChannelSender {

    private static final Logger log = LogManager.getLogger(SendGridEmailSender.class);
    private static final String MAIL_SEND_ENDPOINT = "mail/send";

    private final String apiKey;
    private final String fromAddress;
    private final EmailContentBuilder contentBuilder = new EmailContentBuilder();

    public SendGridEmailSender(
            @Value("${notifications.sendgrid.api-key}") String apiKey,
            @Value("${notifications.email.from}") String fromAddress) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
    }

    @Override
    public String channelName() {
        return "EMAIL";
    }

    @Override
    public boolean send(Notification notification, String destination) {
        try {
            Mail mail = new Mail(
                    new Email(fromAddress),
                    contentBuilder.subject(notification),
                    new Email(destination),
                    new Content("text/html", contentBuilder.htmlBody(notification)));

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND_ENDPOINT);
            request.setBody(mail.build());

            Response response = new SendGrid(apiKey).api(request);
            boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            if (!success) {
                log.warn("SendGrid respondió con estado {} al enviar a {}", response.getStatusCode(), destination);
            }
            return success;
        } catch (IOException e) {
            log.error("Error de E/S al enviar correo por SendGrid a {}", destination, e);
            return false;
        } catch (RuntimeException e) {
            log.error("Error inesperado al enviar correo por SendGrid a {}", destination, e);
            return false;
        }
    }
}
