package com.tuboleta.backend.domain.enums;

/**
 * Estado del evento. REMOVED indica que el evento dejó de aparecer
 * en la fuente (ver Event.missCount, REQ-DET-003).
 */
public enum EventStatus {
    ACTIVE,
    REMOVED
}
