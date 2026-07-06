package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.SearchStatus;
import java.util.List;

/**
 * Búsqueda propia del usuario (REQ-BUS-001/004/005), con el detalle crudo de
 * sus pares proveedor. {@code destinationIds} son los destinos ACTIVOS
 * actualmente asociados (para precargar el multi-select al editar) y
 * {@code eventsCount} es el total de eventos de todos los pares de la
 * búsqueda (para el contador de la card del listado).
 */
public record SearchResponse(
        Long id,
        String term,
        Integer checkFrequencyHours,
        SearchStatus status,
        List<SearchProviderInfo> providers,
        List<Long> destinationIds,
        long eventsCount
) {
}
