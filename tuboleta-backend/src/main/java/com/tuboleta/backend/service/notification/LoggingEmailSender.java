package com.tuboleta.backend.service.notification;

import com.tuboleta.backend.domain.entities.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Envío EMAIL de respaldo (sin API key de SendGrid configurada): no entrega
 * correo real, solo loguea en español lo que se habría enviado. Permite
 * desarrollar y correr {@code contextLoads} sin credenciales (REQ-NOT-005).
 *
 * <p>La condición es el complemento exacto de {@link SendGridEmailSender}
 * (misma property, negada) en vez de {@code ConditionalOnMissingBean}: para
 * beans de {@code @Component} escaneados (no {@code @Configuration @Bean})
 * el orden de evaluación de condiciones no está garantizado por nombre de
 * clase, así que depender de "¿ya existe el otro bean?" es fragil. Con la
 * expresión simétrica, exactamente uno de los dos se activa siempre.</p>
 */
@Component
@ConditionalOnExpression("'${notifications.sendgrid.api-key:}'.isEmpty()")
public class LoggingEmailSender implements ChannelSender {

    private static final Logger log = LogManager.getLogger(LoggingEmailSender.class);

    private final EmailContentBuilder contentBuilder = new EmailContentBuilder();

    @Override
    public String channelName() {
        return "EMAIL";
    }

    @Override
    public boolean send(Notification notification, String destination) {
        String subject = contentBuilder.subject(notification);
        log.info("[Correo simulado, sin SENDGRID_API_KEY] Para: {} - Asunto: {}", destination, subject);
        return true;
    }
}
