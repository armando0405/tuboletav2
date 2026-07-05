package com.tuboleta.backend.domain.entities;

import com.tuboleta.backend.domain.enums.NotificationType;
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
import jakarta.persistence.Transient;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * El hecho notificable (inbox del usuario, REQ-NOT-003). Las entregas
 * por canal viven en NotificationLog; puede existir sin ninguna entrega
 * (solo inbox web). event es NULL para type PROVIDER_DISABLED.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_id", nullable = false)
    private Search search;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Estado leído/no leído del inbox; se marca de forma manual, nunca
     * automáticamente por visitar el inbox.
     */
    @Column(name = "read_at")
    private Instant readAt;

    /**
     * Contexto en memoria (NO persistido) para tipo PROVIDER_DISABLED:
     * la tabla notifications no referencia al proveedor deshabilitado
     * (REQ-FUE-002), así que el nombre y la razón viajan solo mientras
     * dura el fan-out de esta notificación hacia EmailContentBuilder.
     */
    @Transient
    private String providerName;

    @Transient
    private String disabledReason;
}
