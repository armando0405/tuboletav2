package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.AdminUserResponse;
import com.tuboleta.backend.api.dtos.UpdateUserRoleRequest;
import com.tuboleta.backend.api.dtos.UpdateUserStatusRequest;
import com.tuboleta.backend.config.security.AppUserPrincipal;
import com.tuboleta.backend.service.AdminUserService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import com.tuboleta.backend.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administración de usuarios (Fase 1), solo ADMIN — el rol se aplica en
 * {@code SecurityConfig} sobre {@code /api/admin/**}. Las guardas de "no
 * modificarse a sí mismo" viven en el servicio.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ObjectListResponse<AdminUserResponse> list() {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, adminUserService.list());
    }

    @PatchMapping("/{id}/status")
    public ObjectResponse<AdminUserResponse> setStatus(@AuthenticationPrincipal AppUserPrincipal principal,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserStatusRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                adminUserService.setStatus(principal.getId(), id, request.active()));
    }

    @PatchMapping("/{id}/role")
    public ObjectResponse<AdminUserResponse> setRole(@AuthenticationPrincipal AppUserPrincipal principal,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody UpdateUserRoleRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                adminUserService.setRole(principal.getId(), id, request.role()));
    }
}
