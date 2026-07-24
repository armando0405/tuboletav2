package com.tuboleta.backend.service.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * Respaldo transaccional sin Mailgun: no envía, solo loguea. Permite probar el
 * flujo de recuperación en local/tests sin credenciales. Complemento exacto de
 * {@link MailgunEmailService} (misma property, negada).
 */
@Service
@ConditionalOnExpression("'${notifications.mailgun.api-key:}'.isEmpty()")
public class LoggingEmailService implements EmailService {

    private static final Logger log = LogManager.getLogger(LoggingEmailService.class);

    @Override
    public boolean sendHtml(String to, String subject, String htmlBody) {
        log.info("[Correo transaccional SIMULADO - sin MAIL_API_KEY] Para: {} - Asunto: {}", to, subject);
        return true;
    }
}
