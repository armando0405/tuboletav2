package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.domain.entities.NotifChannel;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.domain.entities.NotificationLog;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchNotification;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.entities.UserNotificationChannel;
import com.tuboleta.backend.domain.enums.NotificationType;
import com.tuboleta.backend.domain.enums.SearchStatus;
import com.tuboleta.backend.repository.NotificationLogRepository;
import com.tuboleta.backend.repository.NotificationRepository;
import com.tuboleta.backend.repository.SearchNotificationRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.service.NotificationService;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.notification.ChannelSender;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el hecho notificable (inbox, REQ-NOT-003) y hace el fan-out a los
 * destinos activos del usuario (REQ-NOT-001), entregando por canal detrás de
 * {@link ChannelSender} (REQ-ARQ-004). Nunca lanza por una entrega fallida:
 * queda registrada en {@code notifications_log} con {@code success=false} y
 * el resto de destinos continúa (sin reintentos, futuro declarado).
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LogManager.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final SearchNotificationRepository searchNotificationRepository;
    private final SearchProviderRepository searchProviderRepository;
    private final Map<String, ChannelSender> sendersByChannel;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                    NotificationLogRepository notificationLogRepository,
                                    SearchNotificationRepository searchNotificationRepository,
                                    SearchProviderRepository searchProviderRepository,
                                    List<ChannelSender> channelSenders) {
        this.notificationRepository = notificationRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.searchNotificationRepository = searchNotificationRepository;
        this.searchProviderRepository = searchProviderRepository;
        this.sendersByChannel = channelSenders.stream()
                .collect(Collectors.toMap(ChannelSender::channelName, Function.identity()));
    }

    @Override
    @Transactional
    public void notifyDetectedChanges(SearchProvider pair, List<DetectedChange> changes) {
        Search search = pair.getSearch();
        for (DetectedChange change : changes) {
            Notification notification = Notification.builder()
                    .user(search.getUser())
                    .search(search)
                    .event(change.event())
                    .type(change.type())
                    .createdAt(Instant.now())
                    .build();
            notification = notificationRepository.save(notification);
            log.info("Notificación {} creada: tipo={} búsqueda='{}' usuario={}",
                    notification.getId(), change.type(), search.getTermNormalized(), search.getUser().getId());
            fanOut(notification, search);
        }
    }

    @Override
    @Transactional
    public void notifyProviderDisabled(Provider provider, String reason) {
        List<SearchProvider> pairs = searchProviderRepository.findByProviderId(provider.getId());
        Set<Long> notifiedSearchIds = new HashSet<>();
        for (SearchProvider pair : pairs) {
            Search search = pair.getSearch();
            if (search.getStatus() != SearchStatus.ACTIVE) {
                continue;
            }
            if (!notifiedSearchIds.add(search.getId())) {
                continue;
            }
            Notification notification = Notification.builder()
                    .user(search.getUser())
                    .search(search)
                    .event(null)
                    .type(NotificationType.PROVIDER_DISABLED)
                    .createdAt(Instant.now())
                    .providerName(provider.getName())
                    .disabledReason(reason)
                    .build();
            notification = notificationRepository.save(notification);
            fanOut(notification, search);
        }
    }

    /**
     * Destinos ACTIVOS de la búsqueda (search_notifications) → su destino
     * ACTIVO (user_notification_channels) → si el canal global está ACTIVO
     * (notif_channels), entrega y registra el log. Una notificación sin
     * destinos igual queda persistida (solo inbox, REQ-NOT-003).
     */
    private void fanOut(Notification notification, Search search) {
        List<SearchNotification> destinations = searchNotificationRepository
                .findBySearchIdAndIsActiveTrue(search.getId());
        for (SearchNotification searchNotification : destinations) {
            UserNotificationChannel destination = searchNotification.getUserNotificationChannel();
            if (destination == null || !Boolean.TRUE.equals(destination.getIsActive())) {
                continue;
            }
            NotifChannel channel = destination.getChannel();
            if (channel == null || !Boolean.TRUE.equals(channel.getIsActive())) {
                continue;
            }
            ChannelSender sender = sendersByChannel.get(channel.getName());
            if (sender == null) {
                log.warn("No hay ChannelSender registrado para el canal '{}', se omite la entrega", channel.getName());
                continue;
            }
            deliver(notification, channel, destination.getDestination(), sender);
        }
    }

    private void deliver(Notification notification, NotifChannel channel, String destination, ChannelSender sender) {
        boolean success;
        try {
            success = sender.send(notification, destination);
        } catch (Exception e) {
            log.warn("Fallo al entregar notificación {} por canal '{}' a '{}', se continúa sin reintentar",
                    notification.getId(), channel.getName(), destination, e);
            success = false;
        }
        log.info("Entrega de notificación {} por canal '{}' a '{}': {}",
                notification.getId(), channel.getName(), destination, success ? "OK" : "FALLÓ");
        NotificationLog notificationLog = NotificationLog.builder()
                .notification(notification)
                .channel(channel)
                .destination(destination)
                .sentAt(Instant.now())
                .success(success)
                .build();
        notificationLogRepository.save(notificationLog);
    }
}
