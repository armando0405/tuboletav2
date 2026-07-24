package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Alta de un destino de notificación propio (REQ-NOT-001). Único canal
 * soportado por ahora es EMAIL, así que la dirección se valida como email.
 */
public record DestinationRequest(
        @Email @NotBlank String destination
) {
}
