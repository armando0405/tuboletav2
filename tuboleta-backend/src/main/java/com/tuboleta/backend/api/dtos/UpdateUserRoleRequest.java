package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/** Cambio de rol de un usuario (admin). */
public record UpdateUserRoleRequest(
        @NotNull UserRole role
) {
}
