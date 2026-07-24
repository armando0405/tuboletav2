package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.NotNull;

/** Activar ({@code true}) o desactivar ({@code false}) un usuario (admin). */
public record UpdateUserStatusRequest(
        @NotNull Boolean active
) {
}
