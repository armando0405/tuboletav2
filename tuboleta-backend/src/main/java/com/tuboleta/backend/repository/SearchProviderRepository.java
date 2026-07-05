package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.SearchProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchProviderRepository extends JpaRepository<SearchProvider, Long> {

    /**
     * Pares que usan un proveedor, para notificar PROVIDER_DISABLED
     * (REQ-FUE-002) a las búsquedas afectadas.
     */
    List<SearchProvider> findByProviderId(Long providerId);
}
