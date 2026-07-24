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
 * Histórico de cambios campo a campo (REQ-DET-002): una fila por cada
 * campo extraído que cambió en un evento.
 */
@Entity
@Table(name = "cambios_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Event event;

    @Column(name = "campo", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "detectado_en", nullable = false)
    @Builder.Default
    private Instant detectedAt = Instant.now();
}
