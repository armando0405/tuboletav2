package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.NotificationResponse;
import java.util.List;

/**
 * Consultas del inbox in-app (REQ-NOT-003). Distinto de
 * {@link NotificationService}, que es quien CREA el hecho notificable y hace
 * el fan-out de entrega; este servicio solo LEE y marca leído para el
 * usuario autenticado.
 */
public interface InboxService {

    List<NotificationResponse> listMine(Long userId, boolean unreadOnly);

    long unreadCount(Long userId);

    void markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);
}
