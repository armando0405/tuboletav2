package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.SearchStatus;
import java.util.List;

/**
 * Búsqueda propia del usuario (REQ-BUS-001/004/005), con el detalle crudo de
 * sus pares proveedor.
 */
public record SearchResponse(
        Long id,
        String term,
        Integer checkFrequencyHours,
        SearchStatus status,
        List<SearchProviderInfo> providers
) {
}
