package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.SearchProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchProviderRepository extends JpaRepository<SearchProvider, Long> {

    /**
     * Pares que usan un proveedor, para notificar PROVIDER_DISABLED
     * (REQ-FUE-002) a las búsquedas afectadas.
     */
    List<SearchProvider> findByProviderId(Long providerId);

    /**
     * Candidatos crudos para el scheduler (REQ-DET-004/005): par activo +
     * búsqueda ACTIVE + usuario ACTIVE + proveedor ACTIVE. El filtro de
     * "vencido" (contra {@code last_run_at} y {@code check_frequency_hours})
     * se hace en Java sobre este resultado (volúmenes pequeños,
     * REQ-DET-005) — este método solo aplica los filtros de actividad y
     * hace JOIN FETCH para poder cruzar el hilo del executor sin lazy
     * loading.
     */
    @Query("""
            SELECT sp FROM SearchProvider sp
            JOIN FETCH sp.search s
            JOIN FETCH s.user u
            JOIN FETCH sp.provider p
            WHERE sp.isActive = true
              AND s.status = com.tuboleta.backend.domain.enums.SearchStatus.ACTIVE
              AND u.status = com.tuboleta.backend.domain.enums.UserStatus.ACTIVE
              AND p.status = com.tuboleta.backend.domain.enums.ProviderStatus.ACTIVE
            """)
    List<SearchProvider> findActiveCandidatesForScheduling();
}
