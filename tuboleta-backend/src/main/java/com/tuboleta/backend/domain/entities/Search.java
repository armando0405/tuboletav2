package com.tuboleta.backend.domain.entities;

import com.tuboleta.backend.domain.enums.SearchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Búsqueda configurada por un usuario. El proveedor no vive aquí:
 * una misma búsqueda puede monitorearse en varios proveedores
 * (ver SearchProvider). status DELETED es siempre eliminación lógica.
 */
@Entity
@Table(name = "busquedas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(name = "termino", nullable = false, length = 500)
    private String term;

    /**
     * trim + minúsculas + colapso de espacios (REQ-BUS-002); única por
     * usuario excluyendo búsquedas DELETED (índice parcial en BD).
     */
    @Column(name = "termino_normalizado", nullable = false, length = 500)
    private String termNormalized;

    /** Cada cuántos MINUTOS se monitorea (elegido del catálogo de frecuencias). */
    @Column(name = "frecuencia_minutos", nullable = false)
    @Builder.Default
    private Integer checkFrequencyMinutes = 1440;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private SearchStatus status = SearchStatus.ACTIVE;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
