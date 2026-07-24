package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.EventChange;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventChangeRepository extends JpaRepository<EventChange, Long> {

    /** Historial de cambios de un evento, más recientes primero. */
    List<EventChange> findByEventIdOrderByDetectedAtDesc(Long eventId);

    /**
     * Conteo de cambios por evento en una sola consulta (evita N+1 al armar el
     * listado de eventos). Devuelve filas {@code [eventId, count]}.
     */
    @Query("SELECT ec.event.id, COUNT(ec) FROM EventChange ec WHERE ec.event.id IN :eventIds GROUP BY ec.event.id")
    List<Object[]> countGroupedByEventIds(@Param("eventIds") List<Long> eventIds);
}
