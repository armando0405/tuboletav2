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
 * Destino de notificación reutilizable de un usuario (canal + dirección),
 * asociable a varias búsquedas.
 */
@Entity
@Table(name = "destinos_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id", nullable = false)
    private NotifChannel channel;

    @Column(name = "destino", nullable = false, length = 255)
    private String destination;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
