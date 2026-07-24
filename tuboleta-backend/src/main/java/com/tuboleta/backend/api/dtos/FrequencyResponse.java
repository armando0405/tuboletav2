package com.tuboleta.backend.api.dtos;

/**
 * Frecuencia del catálogo de monitoreo (en minutos). El usuario elige una
 * activa al crear/editar una búsqueda; el admin gestiona el catálogo.
 */
public record FrequencyResponse(
        Long id,
        String label,
        Integer minutes,
        Boolean isActive
) {
}
