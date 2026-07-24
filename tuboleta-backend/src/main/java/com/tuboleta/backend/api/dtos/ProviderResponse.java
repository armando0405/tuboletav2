package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.domain.enums.ProviderType;

/**
 * Vista pública (catálogo) de un proveedor ACTIVE, para que cualquier usuario
 * autenticado lo seleccione al crear una búsqueda (REQ-FUE-001). No expone
 * {@code config} (JSONB interno) ni detalles administrativos como
 * {@code statusReason}/{@code statusChangedAt} — eso queda en
 * {@link ProviderAdminResponse}, solo ADMIN.
 */
public record ProviderResponse(
        Long id,
        String name,
        ProviderType type
) {
}
