package com.tuboleta.backend.domain.entities;

import com.tuboleta.backend.domain.enums.EventStatus;
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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Evento detectado por el scheduler. Histórico INMUTABLE: nunca se
 * borra físicamente. external_id identifica el ítem de forma estable
 * en la fuente (más robusto que el showUniqueId de v1).
 */
@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "busqueda_proveedor_id", nullable = false)
    private SearchProvider searchProvider;

    @Column(name = "id_externo", nullable = false, length = 500)
    private String externalId;

    @Column(name = "titulo", length = 500)
    private String title;

    @Column(name = "lugar", length = 500)
    private String venue;

    @Column(name = "fecha_evento_texto", length = 100)
    private String eventDateRaw;

    @Column(name = "fecha_evento")
    private LocalDate eventDate;

    /**
     * Payload JSON completo del ítem de la fuente.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_crudo")
    private String rawJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.ACTIVE;

    /**
     * Corridas EXITOSAS consecutivas del par sin ver el evento
     * (REQ-DET-003); a las 2 → REMOVED. Una corrida fallida nunca
     * lo incrementa; se resetea a 0 si el evento reaparece.
     */
    @Column(name = "conteo_ausencias", nullable = false)
    @Builder.Default
    private Integer missCount = 0;

    @Column(name = "visto_primera_vez_en", nullable = false)
    @Builder.Default
    private Instant firstSeenAt = Instant.now();

    @Column(name = "visto_ultima_vez_en", nullable = false)
    @Builder.Default
    private Instant lastSeenAt = Instant.now();
}
