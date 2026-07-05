package com.tuboleta.backend.domain.entities;

import com.tuboleta.backend.domain.enums.ProviderStatus;
import com.tuboleta.backend.domain.enums.ProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Catálogo de plataformas de boletas (REQ-FUE-001).
 * status DISABLED (REQ-FUE-002) hace que el scheduler ignore las
 * búsquedas de este proveedor en runtime, sin cascada de borrado.
 */
@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    @Builder.Default
    private ProviderType providerType = ProviderType.SCRAPER;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "search_url", length = 500)
    private String searchUrl;

    /**
     * Configuración flexible por proveedor (user-agent, selectores, endpoints...).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config")
    private String config;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProviderStatus status = ProviderStatus.ACTIVE;

    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;

    @Column(name = "status_changed_at")
    private Instant statusChangedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
