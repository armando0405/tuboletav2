package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Edición del perfil propio (nombre y correo). */
public record UpdateProfileRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}
