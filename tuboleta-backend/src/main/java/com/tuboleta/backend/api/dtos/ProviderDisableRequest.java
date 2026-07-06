package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Motivo de la deshabilitación de un proveedor (REQ-FUE-002), texto libre
 * que explica el porqué (y el matiz temporal/permanente).
 */
public record ProviderDisableRequest(
        @NotBlank String reason
) {
}
