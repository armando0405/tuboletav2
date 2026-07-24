package com.tuboleta.backend.service;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.service.detection.DetectedChange;
import java.util.List;

/**
 * Crea el hecho notificable (inbox, REQ-NOT-003) y hace el fan-out a los
 * destinos activos del usuario (REQ-NOT-001) entregando por el canal
 * correspondiente (REQ-NOT-002, REQ-ARQ-004). La falla de un envío nunca
 * rompe el flujo: queda registrada como entrega fallida y se continúa.
 */
public interface NotificationService {

    /**
     * Persiste una {@code Notification} por cada cambio detectado
     * (NEW/CHANGED/REMOVED) y hace el fan-out a los destinos activos de la
     * búsqueda del par. Una notificación sin destinos igual queda en el
     * inbox (REQ-NOT-003).
     *
     * @param pair    par búsqueda↔proveedor sobre el que se detectaron los cambios
     * @param changes hechos notificables devueltos por {@code ChangeDetectionService}
     */
    void notifyDetectedChanges(SearchProvider pair, List<DetectedChange> changes);

    /**
     * Notifica PROVIDER_DISABLED (REQ-FUE-002): una {@code Notification} por
     * cada búsqueda ACTIVA que tenga un par con este proveedor, con el mismo
     * fan-out. {@code event} queda NULL.
     *
     * @param provider proveedor que fue deshabilitado
     * @param reason   texto libre con el motivo (status_reason)
     */
    void notifyProviderDisabled(Provider provider, String reason);
}
