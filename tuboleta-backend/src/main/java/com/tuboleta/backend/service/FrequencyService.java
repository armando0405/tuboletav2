package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.FrequencyRequest;
import com.tuboleta.backend.api.dtos.FrequencyResponse;
import java.util.List;

/**
 * Catálogo de frecuencias de monitoreo (REQ-BUS-005). Las de lectura son para
 * el select del usuario; las de escritura para el panel admin.
 */
public interface FrequencyService {

    /** Frecuencias ACTIVAS, para que el usuario elija al crear/editar una búsqueda. */
    List<FrequencyResponse> listActive();

    /** Todas las frecuencias (activas e inactivas), para el panel admin. */
    List<FrequencyResponse> listAll();

    FrequencyResponse create(FrequencyRequest request);

    FrequencyResponse update(Long id, FrequencyRequest request);

    void delete(Long id);
}
