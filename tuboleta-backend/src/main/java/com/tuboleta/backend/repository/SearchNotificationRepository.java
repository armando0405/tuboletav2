package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.SearchNotification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchNotificationRepository extends JpaRepository<SearchNotification, Long> {

    /**
     * Destinos activos de una búsqueda (fan-out, REQ-NOT-001). El filtro de
     * canal/destino activos se hace en el servicio, no aquí.
     */
    List<SearchNotification> findBySearchIdAndIsActiveTrue(Long searchId);
}
