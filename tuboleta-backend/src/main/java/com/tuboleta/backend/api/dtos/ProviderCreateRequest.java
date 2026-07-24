package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Alta de una fuente desde el panel admin (Fase multi-sitio). {@code searchUrl}
 * debe contener {@code {term}} (se reemplaza por el término normalizado);
 * {@code config} es el JSON con selectores (SCRAPER) o rutas (API) — puede ir
 * vacío/null.
 */
public record ProviderCreateRequest(
        @NotBlank String name,
        @NotNull ProviderType providerType,
        @NotBlank String baseUrl,
        @NotBlank String searchUrl,
        String config
) {
}
