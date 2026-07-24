package com.tuboleta.backend.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Log de corridas del scheduler (REQ-DET-005): una fila por petición
 * real al proveedor. success = false → no incrementa miss_count ni
 * genera REMOVED (REQ-DET-003), aunque sus eventos sí se procesan.
 */
@Entity
@Table(name = "corridas_proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Provider provider;

    @Column(name = "termino_normalizado", nullable = false, length = 500)
    private String termNormalized;

    @Column(name = "iniciado_en", nullable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "finalizado_en")
    private Instant finishedAt;

    @Column(name = "exito")
    private Boolean success;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "eventos_encontrados")
    private Integer eventsFound;

    @Column(name = "pares_aplicados")
    private Integer pairsApplied;
}
