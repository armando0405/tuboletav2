package com.tuboleta.backend.api.dtos;

/**
 * Destino de notificación propio (REQ-NOT-001). No documentado literalmente
 * en el brief de Task 7, pero necesario como respuesta de
 * {@code DestinationController} (nunca exponer la entidad JPA).
 */
public record DestinationResponse(
        Long id,
        String channelName,
        String destination,
        Boolean isActive
) {
}
