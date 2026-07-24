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
 * Relación N:M búsqueda ↔ proveedor. Cada par se ejecuta y pausa de
 * forma independiente; last_run_at es la última corrida de ESTE par.
 */
@Entity
@Table(name = "busqueda_proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "busqueda_id", nullable = false)
    private Search search;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Provider provider;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "ultima_corrida_en")
    private Instant lastRunAt;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
