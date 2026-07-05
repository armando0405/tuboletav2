package com.tuboleta.backend.service.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.enums.NotificationType;
import org.junit.jupiter.api.Test;

/**
 * Caso 6 del brief de Task 5: asunto correcto por tipo, instanciado
 * directamente (sin Spring, sin mocks).
 */
class EmailContentBuilderTest {

    private final EmailContentBuilder builder = new EmailContentBuilder();

    private Search search() {
        return Search.builder().id(1L).term("Rock Fest").build();
    }

    @Test
    void subjectForNew_mentionsSearchTerm() {
        Notification notification = Notification.builder()
                .search(search()).type(NotificationType.NEW)
                .event(Event.builder().externalId("/e/1").title("Rock Fest 2026").build())
                .build();

        assertThat(builder.subject(notification))
                .isEqualTo("[TuBoleta] Nuevo evento para tu búsqueda «Rock Fest»");
    }

    @Test
    void subjectForChanged_mentionsSearchTerm() {
        Notification notification = Notification.builder()
                .search(search()).type(NotificationType.CHANGED)
                .event(Event.builder().externalId("/e/1").title("Rock Fest 2026").build())
                .build();

        assertThat(builder.subject(notification)).isEqualTo("[TuBoleta] Un evento de «Rock Fest» cambió");
    }

    @Test
    void subjectForRemoved_mentionsSearchTerm() {
        Notification notification = Notification.builder()
                .search(search()).type(NotificationType.REMOVED)
                .event(Event.builder().externalId("/e/1").title("Rock Fest 2026").build())
                .build();

        assertThat(builder.subject(notification))
                .isEqualTo("[TuBoleta] Un evento de «Rock Fest» ya no está disponible");
    }

    @Test
    void subjectForProviderDisabled_mentionsProviderName() {
        Notification notification = Notification.builder()
                .search(search()).type(NotificationType.PROVIDER_DISABLED)
                .providerName("TuBoleta")
                .build();

        assertThat(builder.subject(notification)).isEqualTo("[TuBoleta] La fuente TuBoleta fue deshabilitada");
    }

    @Test
    void bodyForEvent_includesTitleAndLink() {
        Notification notification = Notification.builder()
                .search(search()).type(NotificationType.NEW)
                .event(Event.builder().externalId("/es/eventos/rock-fest").title("Rock Fest 2026")
                        .venue("Movistar Arena").build())
                .build();

        String body = builder.htmlBody(notification);

        assertThat(body).contains("Rock Fest 2026");
        assertThat(body).contains("Movistar Arena");
        assertThat(body).contains("https://www.tuboleta.com/es/eventos/rock-fest");
    }

    @Test
    void bodyForProviderDisabled_includesReason() {
        Notification notification = Notification.builder()
                .search(search()).type(NotificationType.PROVIDER_DISABLED)
                .providerName("TuBoleta")
                .disabledReason("Fallas repetidas de conexión")
                .build();

        String body = builder.htmlBody(notification);

        assertThat(body).contains("TuBoleta");
        assertThat(body).contains("Fallas repetidas de conexión");
    }
}
