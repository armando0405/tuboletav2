package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.NotificationType;
import java.time.Instant;

/**
 * Fila del inbox (REQ-NOT-003): {@code readAt == null} es "no leída".
 * {@code eventTitle} es {@code null} para PROVIDER_DISABLED (sin evento).
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        String searchTerm,
        String eventTitle,
        Instant createdAt,
        Instant readAt
) {
}
