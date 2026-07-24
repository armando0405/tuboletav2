package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Solicitud de recuperación de contraseña (endpoint público). */
public record ForgotPasswordRequest(
        @Email @NotBlank String email
) {
}
