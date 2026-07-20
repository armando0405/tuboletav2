package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.Frequency;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrequencyRepository extends JpaRepository<Frequency, Long> {

    /** Frecuencias activas del catálogo, para el select del usuario. */
    List<Frequency> findByIsActiveTrueOrderByMinutesAsc();

    /** Todas, ordenadas, para el panel admin. */
    List<Frequency> findAllByOrderByMinutesAsc();

    /** Validar que un valor en minutos existe y está activo en el catálogo. */
    Optional<Frequency> findByMinutesAndIsActiveTrue(Integer minutes);

    /** Unicidad del valor en minutos (activo o no), para el alta/edición admin. */
    Optional<Frequency> findByMinutes(Integer minutes);
}
