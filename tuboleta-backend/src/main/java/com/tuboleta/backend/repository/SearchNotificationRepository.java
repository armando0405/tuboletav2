package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.SearchNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchNotificationRepository extends JpaRepository<SearchNotification, Long> {

    /**
     * Destinos activos de una búsqueda (fan-out, REQ-NOT-001). El filtro de
     * canal/destino activos se hace en el servicio, no aquí.
     */
    List<SearchNotification> findBySearchIdAndIsActiveTrue(Long searchId);

    /**
     * Todas las filas (activas o no) de una búsqueda, para reconciliar el
     * conjunto de destinos en un PATCH (Task 7).
     */
    List<SearchNotification> findBySearchId(Long searchId);

    /**
     * Espeja {@code uq_search_notif_destination}: existe ya una fila para
     * este par (activa o no), para reactivar en vez de duplicar.
     */
    Optional<SearchNotification> findBySearchIdAndUserNotificationChannelId(Long searchId, Long userNotificationChannelId);
}
