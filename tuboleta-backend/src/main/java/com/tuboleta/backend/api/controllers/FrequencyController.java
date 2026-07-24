package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.FrequencyResponse;
import com.tuboleta.backend.service.FrequencyService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catálogo de frecuencias ACTIVAS para el select del usuario al crear/editar
 * una búsqueda (REQ-BUS-005). Autenticado; la gestión del catálogo es
 * ADMIN-only ({@code /api/admin/frequencies}).
 */
@RestController
@RequestMapping("/api/frequencies")
public class FrequencyController {

    private final FrequencyService frequencyService;

    public FrequencyController(FrequencyService frequencyService) {
        this.frequencyService = frequencyService;
    }

    @GetMapping
    public ObjectListResponse<FrequencyResponse> listActive() {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, frequencyService.listActive());
    }
}
