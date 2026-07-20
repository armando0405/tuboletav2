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
 * Auditoría inmutable de entregas por canal (tabla notifications_log).
 * destination se guarda como snapshot de texto: conserva a dónde se
 * envió realmente aunque el destino original cambie o se elimine.
 */
@Entity
@Table(name = "notificaciones_bitacora")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id", nullable = false)
    private NotifChannel channel;

    @Column(name = "destino", nullable = false, length = 255)
    private String destination;

    @Column(name = "enviado_en", nullable = false)
    @Builder.Default
    private Instant sentAt = Instant.now();

    @Column(name = "exito", nullable = false)
    @Builder.Default
    private Boolean success = true;
}
