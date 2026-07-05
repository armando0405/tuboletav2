package com.tuboleta.backend.domain.enums;

/**
 * Estado del proveedor (REQ-FUE-002). DISABLED hace que el scheduler
 * ignore sus búsquedas en runtime; reactivación solo por ADMIN.
 */
public enum ProviderStatus {
    ACTIVE,
    DISABLED
}
