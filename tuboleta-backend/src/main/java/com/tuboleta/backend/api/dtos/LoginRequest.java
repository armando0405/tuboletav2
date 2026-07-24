package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de login (REQ-USU-002).
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
