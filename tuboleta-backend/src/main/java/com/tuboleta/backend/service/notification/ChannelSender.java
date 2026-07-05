package com.tuboleta.backend.service.notification;

import com.tuboleta.backend.domain.entities.Notification;

/**
 * Abstracción de envío por canal (REQ-ARQ-004): agregar Telegram/WhatsApp es
 * agregar una implementación nueva, sin tocar {@code NotificationService}.
 */
public interface ChannelSender {

    /**
     * Nombre del canal que maneja esta implementación (debe coincidir con
     * {@code notif_channels.name}, ej. "EMAIL").
     */
    String channelName();

    /**
     * Entrega la notificación al destino dado. No lanza: cualquier fallo de
     * entrega debe traducirse en {@code false} (el llamador registra la
     * entrega fallida y continúa, sin reintentos).
     *
     * @param notification hecho notificable ya persistido
     * @param destination   destino snapshot (email, chat_id, teléfono...)
     * @return true si la entrega fue exitosa
     */
    boolean send(Notification notification, String destination);
}
