package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Todos los eventos (cualquier estado) ya registrados para un par
     * búsqueda↔proveedor, usados por la detección de cambios (REQ-DET-001)
     * para distinguir ítems nuevos de conocidos.
     */
    List<Event> findBySearchProviderId(Long searchProviderId);
}
