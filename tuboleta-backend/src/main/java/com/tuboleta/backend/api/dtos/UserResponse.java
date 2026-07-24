package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.UserRole;

/**
 * Usuario autenticado (register/login/me). Nunca expone {@code password_hash}.
 */
public record UserResponse(
        Long id,
        String email,
        String name,
        UserRole role
) {
}
