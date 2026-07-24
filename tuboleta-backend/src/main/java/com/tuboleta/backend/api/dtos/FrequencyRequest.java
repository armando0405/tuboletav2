package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Alta/edición de una frecuencia del catálogo (panel admin). {@code minutes}
 * es único en el catálogo; {@code isActive} opcional (por defecto activa).
 */
public record FrequencyRequest(
        @NotBlank String label,
        @NotNull @Positive Integer minutes,
        Boolean isActive
) {
}
