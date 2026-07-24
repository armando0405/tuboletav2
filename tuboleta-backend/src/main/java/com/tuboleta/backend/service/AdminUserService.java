package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.AdminUserResponse;
import com.tuboleta.backend.domain.enums.UserRole;
import java.util.List;

/**
 * Gestión de usuarios para ADMIN (Fase 1). El {@code actingUserId} sirve para
 * impedir que un admin se bloquee a sí mismo (no puede cambiar su propio estado
 * ni su propio rol).
 */
public interface AdminUserService {

    List<AdminUserResponse> list();

    AdminUserResponse setStatus(Long actingUserId, Long targetUserId, boolean active);

    AdminUserResponse setRole(Long actingUserId, Long targetUserId, UserRole role);
}
