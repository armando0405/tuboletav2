package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Inbox del usuario (REQ-NOT-003), más reciente primero.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadAtIsNull(Long userId);

    /**
     * Ownership: marcar como leída una notificación ajena se trata como
     * inexistente.
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    List<Notification> findByUserIdAndReadAtIsNull(Long userId);
}
