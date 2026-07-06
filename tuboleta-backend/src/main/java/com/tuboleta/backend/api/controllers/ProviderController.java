package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.ProviderResponse;
import com.tuboleta.backend.service.ProviderService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catálogo público de fuentes ACTIVE (REQ-FUE-001), para cualquier usuario
 * autenticado al elegir proveedor(es) en una búsqueda. Distinto de
 * {@code /api/admin/providers} (solo ADMIN, incluye DISABLED y campos
 * administrativos).
 */
@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public ObjectListResponse<ProviderResponse> list() {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, providerService.listActive());
    }
}
