package com.tuboleta.backend.service.detection;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.enums.NotificationType;

/**
 * Hecho notificable producido por {@link com.tuboleta.backend.service.ChangeDetectionService}
 * (REQ-DET-001/002/003). No envía notificaciones: solo describe qué pasó con
 * un evento en la corrida actual, para que otra capa (REQ-ARQ-004) decida
 * cómo notificarlo.
 *
 * @param type  NEW (nuevo o reaparecido), CHANGED o REMOVED — nunca PROVIDER_DISABLED aquí
 * @param event el evento afectado, ya persistido con su estado actualizado
 */
public record DetectedChange(NotificationType type, Event event) {
}
