package com.tuboleta.backend.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de frecuencias de monitoreo permitidas (en minutos), gestionable
 * por el admin. El usuario elige una de las activas al crear/editar una
 * búsqueda; el valor en minutos se copia a {@code searches.check_frequency_minutes}.
 */
@Entity
@Table(name = "frequencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Frequency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "label", nullable = false, length = 60)
    private String label;

    @Column(name = "minutes", nullable = false)
    private Integer minutes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
