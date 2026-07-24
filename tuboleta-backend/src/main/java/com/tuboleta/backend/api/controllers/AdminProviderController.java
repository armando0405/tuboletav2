package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.ProviderAdminResponse;
import com.tuboleta.backend.api.dtos.ProviderCreateRequest;
import com.tuboleta.backend.api.dtos.ProviderDisableRequest;
import com.tuboleta.backend.api.dtos.ProviderUpdateRequest;
import com.tuboleta.backend.service.AdminProviderService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import com.tuboleta.backend.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administración de fuentes (REQ-FUE-001/002), solo ADMIN — el rol se aplica
 * en {@code SecurityConfig} sobre {@code /api/admin/**}, no aquí.
 */
@RestController
@RequestMapping("/api/admin/providers")
public class AdminProviderController {

    private final AdminProviderService adminProviderService;

    public AdminProviderController(AdminProviderService adminProviderService) {
        this.adminProviderService = adminProviderService;
    }

    @GetMapping
    public ObjectListResponse<ProviderAdminResponse> list() {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, adminProviderService.list());
    }

    @PostMapping
    public ObjectResponse<ProviderAdminResponse> create(@Valid @RequestBody ProviderCreateRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, adminProviderService.create(request));
    }

    @PatchMapping("/{id}")
    public ObjectResponse<ProviderAdminResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody ProviderUpdateRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, adminProviderService.update(id, request));
    }

    @PostMapping("/{id}/disable")
    public ObjectResponse<ProviderAdminResponse> disable(@PathVariable Long id,
                                                          @Valid @RequestBody ProviderDisableRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                adminProviderService.disable(id, request.reason()));
    }

    @PostMapping("/{id}/enable")
    public ObjectResponse<ProviderAdminResponse> enable(@PathVariable Long id) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, adminProviderService.enable(id));
    }
}
