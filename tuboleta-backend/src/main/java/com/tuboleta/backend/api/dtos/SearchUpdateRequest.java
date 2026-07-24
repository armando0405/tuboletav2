package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Edición parcial de una búsqueda (REQ-BUS-005): ambos campos son opcionales —
 * {@code null} significa "no tocar este campo". La frecuencia va en MINUTOS y,
 * si se envía, debe existir en el catálogo activo (lo valida el servicio).
 * {@code destinationIds} reemplaza el conjunto completo de destinos activos.
 */
public record SearchUpdateRequest(
        @Positive Integer checkFrequencyMinutes,
        List<Long> destinationIds
) {
}
