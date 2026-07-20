package com.tuboleta.backend.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Alta de una búsqueda (REQ-BUS-001/002/005): el término se normaliza en el
 * servicio ({@code TermNormalizer}); la frecuencia va en MINUTOS y debe existir
 * en el catálogo activo (lo valida el servicio contra {@code frequencies}); al
 * menos un proveedor es obligatorio (destinos no).
 */
public record SearchCreateRequest(
        @NotBlank String term,
        @NotNull @Positive Integer checkFrequencyMinutes,
        @NotEmpty List<Long> providerIds,
        List<Long> destinationIds
) {
}
