package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.NotificationType;
import java.time.Instant;

/**
 * Fila del inbox (REQ-NOT-003): {@code readAt == null} es "no leída".
 * {@code eventTitle}/{@code eventId} son {@code null} para PROVIDER_DISABLED
 * (sin evento). {@code searchId}/{@code eventId} permiten cruzar la novedad con
 * su evento por identificador (no por texto) en el frontend.
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        Long searchId,
        String searchTerm,
        Long eventId,
        String eventTitle,
        Instant createdAt,
        Instant readAt
) {
}
