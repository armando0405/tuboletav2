package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.enums.SearchStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends JpaRepository<Search, Long> {

    /**
     * Búsquedas propias del usuario, excluyendo las eliminadas lógicamente
     * (REQ-BUS-004).
     */
    List<Search> findByUserIdAndStatusNot(Long userId, SearchStatus status);

    /**
     * Ownership: un recurso ajeno se trata como inexistente
     * (nunca se filtra su existencia a otro usuario).
     */
    Optional<Search> findByIdAndUserId(Long id, Long userId);

    /**
     * Unicidad por término normalizado excluyendo DELETED (REQ-BUS-002):
     * espeja el índice único parcial de {@code searches}.
     */
    Optional<Search> findByUserIdAndTermNormalizedAndStatusNot(Long userId, String termNormalized, SearchStatus status);
}
