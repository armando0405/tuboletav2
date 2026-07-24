package com.tuboleta.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.NotifChannel;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.domain.entities.NotificationLog;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchNotification;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.domain.entities.UserNotificationChannel;
import com.tuboleta.backend.domain.enums.NotificationType;
import com.tuboleta.backend.domain.enums.SearchStatus;
import com.tuboleta.backend.repository.NotificationLogRepository;
import com.tuboleta.backend.repository.NotificationRepository;
import com.tuboleta.backend.repository.SearchNotificationRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.notification.ChannelSender;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos mínimos del brief de Task 5: fan-out a destinos (REQ-NOT-001),
 * canal activo (REQ-NOT-002) y PROVIDER_DISABLED (REQ-FUE-002).
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private SearchNotificationRepository searchNotificationRepository;

    @Mock
    private SearchProviderRepository searchProviderRepository;

    @Mock
    private ChannelSender emailSender;

    private NotificationServiceImpl service;

    private User user;
    private Search search;
    private SearchProvider pair;
    private NotifChannel emailChannel;

    @BeforeEach
    void setUp() {
        when(emailSender.channelName()).thenReturn("EMAIL");
        service = new NotificationServiceImpl(notificationRepository, notificationLogRepository,
                searchNotificationRepository, searchProviderRepository, List.of(emailSender));

        user = User.builder().id(1L).email("dueno@correo.com").name("Dueño").build();
        search = Search.builder().id(10L).user(user).term("Rock Fest").termNormalized("rock fest").build();
        pair = SearchProvider.builder().id(100L).search(search).build();
        emailChannel = NotifChannel.builder().id(1L).name("EMAIL").isActive(true).build();

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private UserNotificationChannel destination(long id, String address, boolean active) {
        return UserNotificationChannel.builder()
                .id(id)
                .user(user)
                .channel(emailChannel)
                .destination(address)
                .isActive(active)
                .build();
    }

    private SearchNotification association(UserNotificationChannel destination) {
        return SearchNotification.builder()
                .id(destination.getId())
                .search(search)
                .userNotificationChannel(destination)
                .isActive(true)
                .build();
    }

    @Test
    void newChangeWithTwoActiveDestinations_persistsOneNotificationAndTwoSuccessfulLogs() {
        Event event = Event.builder().id(50L).externalId("/e/1").title("Rock Fest 2026").build();
        UserNotificationChannel d1 = destination(1L, "a@correo.com", true);
        UserNotificationChannel d2 = destination(2L, "b@correo.com", true);
        when(searchNotificationRepository.findBySearchIdAndIsActiveTrue(10L))
                .thenReturn(List.of(association(d1), association(d2)));
        when(emailSender.send(any(), any())).thenReturn(true);

        service.notifyDetectedChanges(pair, List.of(new DetectedChange(NotificationType.NEW, event)));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getType()).isEqualTo(NotificationType.NEW);
        assertThat(notificationCaptor.getValue().getEvent()).isSameAs(event);
        assertThat(notificationCaptor.getValue().getUser()).isSameAs(user);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(2)).save(logCaptor.capture());
        List<NotificationLog> logs = logCaptor.getAllValues();
        assertThat(logs).extracting(NotificationLog::getDestination)
                .containsExactlyInAnyOrder("a@correo.com", "b@correo.com");
        assertThat(logs).allMatch(NotificationLog::getSuccess);
        assertThat(logs).allMatch(l -> l.getChannel() == emailChannel);
    }

    @Test
    void inactiveDestination_isSkipped_onlyOneLogInsteadOfTwo() {
        Event event = Event.builder().id(50L).externalId("/e/1").title("Rock Fest 2026").build();
        UserNotificationChannel active = destination(1L, "a@correo.com", true);
        UserNotificationChannel inactive = destination(2L, "b@correo.com", false);
        when(searchNotificationRepository.findBySearchIdAndIsActiveTrue(10L))
                .thenReturn(List.of(association(active), association(inactive)));
        when(emailSender.send(any(), any())).thenReturn(true);

        service.notifyDetectedChanges(pair, List.of(new DetectedChange(NotificationType.NEW, event)));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getDestination()).isEqualTo("a@correo.com");
    }

    @Test
    void noDestinations_notificationIsPersisted_zeroLogs() {
        Event event = Event.builder().id(50L).externalId("/e/1").title("Rock Fest 2026").build();
        when(searchNotificationRepository.findBySearchIdAndIsActiveTrue(10L)).thenReturn(List.of());

        service.notifyDetectedChanges(pair, List.of(new DetectedChange(NotificationType.NEW, event)));

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(notificationLogRepository, never()).save(any());
    }

    @Test
    void senderThrowsOrReturnsFalse_logsFailureAndContinuesWithRemainingDestinations() {
        Event event = Event.builder().id(50L).externalId("/e/1").title("Rock Fest 2026").build();
        UserNotificationChannel failing = destination(1L, "fail@correo.com", true);
        UserNotificationChannel ok = destination(2L, "ok@correo.com", true);
        when(searchNotificationRepository.findBySearchIdAndIsActiveTrue(10L))
                .thenReturn(List.of(association(failing), association(ok)));
        when(emailSender.send(any(), org.mockito.ArgumentMatchers.eq("fail@correo.com")))
                .thenThrow(new RuntimeException("SendGrid caído"));
        when(emailSender.send(any(), org.mockito.ArgumentMatchers.eq("ok@correo.com"))).thenReturn(true);

        service.notifyDetectedChanges(pair, List.of(new DetectedChange(NotificationType.NEW, event)));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(2)).save(logCaptor.capture());
        List<NotificationLog> logs = logCaptor.getAllValues();
        NotificationLog failedLog = logs.stream()
                .filter(l -> l.getDestination().equals("fail@correo.com")).findFirst().orElseThrow();
        NotificationLog okLog = logs.stream()
                .filter(l -> l.getDestination().equals("ok@correo.com")).findFirst().orElseThrow();
        assertThat(failedLog.getSuccess()).isFalse();
        assertThat(okLog.getSuccess()).isTrue();
    }

    @Test
    void providerDisabled_withTwoAffectedActiveSearches_createsTwoNotificationsWithNullEvent() {
        Provider provider = Provider.builder().id(5L).name("TuBoleta").build();

        Search search1 = Search.builder().id(10L).user(user).term("Rock Fest").status(SearchStatus.ACTIVE).build();
        Search search2 = Search.builder().id(11L).user(user).term("Jazz Fest").status(SearchStatus.ACTIVE).build();
        SearchProvider pair1 = SearchProvider.builder().id(100L).search(search1).provider(provider).build();
        SearchProvider pair2 = SearchProvider.builder().id(101L).search(search2).provider(provider).build();

        when(searchProviderRepository.findByProviderId(5L)).thenReturn(List.of(pair1, pair2));
        when(searchNotificationRepository.findBySearchIdAndIsActiveTrue(10L)).thenReturn(List.of());
        when(searchNotificationRepository.findBySearchIdAndIsActiveTrue(11L)).thenReturn(List.of());

        service.notifyProviderDisabled(provider, "Fallas repetidas de conexión");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        List<Notification> saved = captor.getAllValues();
        assertThat(saved).allMatch(n -> n.getType() == NotificationType.PROVIDER_DISABLED);
        assertThat(saved).allMatch(n -> n.getEvent() == null);
        assertThat(saved).extracting(n -> n.getSearch().getId()).containsExactlyInAnyOrder(10L, 11L);
    }
}
