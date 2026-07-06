package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.NotificationResponse;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.repository.NotificationRepository;
import com.tuboleta.backend.service.InboxService;
import com.tuboleta.backend.utils.exception.NotFoundRegisterException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lectura del inbox y marcado de leído (REQ-NOT-003): nunca se marca leído
 * automáticamente por listar/visitar, solo por acción explícita.
 */
@Service
public class InboxServiceImpl implements InboxService {

    private final NotificationRepository notificationRepository;

    public InboxServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(Long userId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(InboxServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundRegisterException("Notificacion", "id", notificationId));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadAtIsNull(userId);
        Instant now = Instant.now();
        for (Notification notification : unread) {
            notification.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    private static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getSearch() != null ? notification.getSearch().getTerm() : null,
                notification.getEvent() != null ? notification.getEvent().getTitle() : null,
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
