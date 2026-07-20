package com.tuboleta.backend.api.dtos;

import java.time.Instant;

/**
 * Una entrada del historial de cambios de un evento (REQ-DET-002): qué campo
 * cambió, su valor anterior y el nuevo, y cuándo se detectó. {@code fieldLabel}
 * es la etiqueta en español lista para mostrar.
 */
public record EventChangeResponse(
        Long id,
        String fieldName,
        String fieldLabel,
        String oldValue,
        String newValue,
        Instant detectedAt
) {
}
