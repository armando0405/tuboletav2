package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.UserNotificationChannel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationChannelRepository extends JpaRepository<UserNotificationChannel, Long> {

    /**
     * Destinos propios del usuario (REQ-NOT-001).
     */
    List<UserNotificationChannel> findByUserId(Long userId);

    /**
     * Ownership: un destino ajeno se trata como inexistente.
     */
    Optional<UserNotificationChannel> findByIdAndUserId(Long id, Long userId);

    /**
     * Espeja {@code uq_user_channel_destination} para detectar duplicados
     * antes del INSERT.
     */
    Optional<UserNotificationChannel> findByUserIdAndChannelIdAndDestination(Long userId, Long channelId, String destination);
}
