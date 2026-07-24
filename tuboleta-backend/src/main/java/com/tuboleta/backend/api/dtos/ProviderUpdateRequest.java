package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Edición de una fuente existente (no cambia su estado ACTIVE/DISABLED). */
public record ProviderUpdateRequest(
        @NotBlank String name,
        @NotNull ProviderType providerType,
        @NotBlank String baseUrl,
        @NotBlank String searchUrl,
        String config
) {
}
