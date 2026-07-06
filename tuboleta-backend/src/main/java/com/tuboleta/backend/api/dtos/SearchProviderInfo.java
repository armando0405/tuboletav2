package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.ProviderStatus;
import java.time.Instant;

/**
 * Un par búsqueda↔proveedor dentro de {@link SearchResponse}. El estado
 * EFECTIVO (pausado por el usuario vs. deshabilitado por el ADMIN) lo arma
 * el frontend combinando {@code pairActive} y {@code providerStatus}
 * (REQ-FE-002) — este backend solo entrega los datos crudos.
 */
public record SearchProviderInfo(
        Long providerId,
        String providerName,
        Boolean pairActive,
        ProviderStatus providerStatus,
        Instant lastRunAt
) {
}
