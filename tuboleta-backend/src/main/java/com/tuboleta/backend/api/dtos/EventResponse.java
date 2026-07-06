package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.EventStatus;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Evento detectado para un par de la búsqueda (REQ-DET-001/003), con el
 * nombre del proveedor resuelto para no obligar al frontend a otro join.
 */
public record EventResponse(
        Long id,
        String providerName,
        String externalId,
        String title,
        String venue,
        String eventDateRaw,
        LocalDate eventDate,
        EventStatus status,
        Integer missCount,
        Instant firstSeenAt,
        Instant lastSeenAt
) {
}
