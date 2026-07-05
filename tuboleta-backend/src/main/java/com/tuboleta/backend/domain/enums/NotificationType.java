package com.tuboleta.backend.domain.enums;

/**
 * Tipo de notificación (REQ-NOT-003): NEW (evento nuevo o reaparecido),
 * CHANGED (cambio detectado), REMOVED (evento desapareció de la fuente),
 * PROVIDER_DISABLED (fuente deshabilitada por ADMIN, sin evento asociado).
 */
public enum NotificationType {
    NEW,
    CHANGED,
    REMOVED,
    PROVIDER_DISABLED
}
