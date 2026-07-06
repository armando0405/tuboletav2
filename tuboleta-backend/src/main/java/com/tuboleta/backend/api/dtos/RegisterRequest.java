package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta de un USER (REQ-USU-001/002); AuthController siempre crea rol USER.
 */
public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String name,
        @Size(min = 8) String password
) {
}
