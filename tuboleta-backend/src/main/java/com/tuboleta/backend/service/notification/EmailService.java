package com.tuboleta.backend.service.notification;

/**
 * Envío de correos "transaccionales" (no ligados a una {@code Notification}),
 * como el enlace de recuperación de contraseña. Complementa al
 * {@link ChannelSender} del flujo de notificaciones: mismo transporte
 * (Mailgun/Logging según haya API key), distinta puerta de entrada.
 */
public interface EmailService {

    /**
     * @return {@code true} si el proveedor aceptó el envío.
     */
    boolean sendHtml(String to, String subject, String htmlBody);
}
