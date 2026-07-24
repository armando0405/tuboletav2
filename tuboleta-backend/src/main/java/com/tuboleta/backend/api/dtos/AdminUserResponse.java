package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.UserRole;
import com.tuboleta.backend.domain.enums.UserStatus;
import java.time.Instant;

/** Fila del panel admin de usuarios (incluye estado y fecha de alta). */
public record AdminUserResponse(
        Long id,
        String email,
        String name,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {
}
