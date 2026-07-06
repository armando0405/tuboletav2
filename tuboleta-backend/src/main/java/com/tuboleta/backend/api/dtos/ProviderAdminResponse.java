package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.ProviderStatus;
import com.tuboleta.backend.domain.enums.ProviderType;
import java.time.Instant;

/**
 * Vista administrativa de un proveedor (REQ-FUE-001/002), solo para ADMIN.
 */
public record ProviderAdminResponse(
        Long id,
        String name,
        ProviderType providerType,
        String baseUrl,
        ProviderStatus status,
        String statusReason,
        Instant statusChangedAt
) {
}
