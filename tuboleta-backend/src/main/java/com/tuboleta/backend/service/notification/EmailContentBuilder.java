package com.tuboleta.backend.service.notification;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.enums.NotificationType;

/**
 * Genera asunto y cuerpo HTML simple (sin motor de plantillas) del correo de
 * notificación, en español, a partir del hecho notificable. Sin estado: se
 * puede instanciar libremente ({@code new EmailContentBuilder()}).
 */
public class EmailContentBuilder {

    private static final String SUBJECT_PREFIX = "[TuBoleta] ";
    private static final String EVENT_LINK_BASE = "https://www.tuboleta.com";

    /**
     * Asunto del correo según el tipo de notificación (REQ-NOT-002).
     */
    public String subject(Notification notification) {
        NotificationType type = notification.getType();
        String term = searchTerm(notification);
        return switch (type) {
            case NEW -> SUBJECT_PREFIX + "Nuevo evento para tu búsqueda «" + term + "»";
            case CHANGED -> SUBJECT_PREFIX + "Un evento de «" + term + "» cambió";
            case REMOVED -> SUBJECT_PREFIX + "Un evento de «" + term + "» ya no está disponible";
            case PROVIDER_DISABLED -> SUBJECT_PREFIX + "La fuente " + notification.getProviderName()
                    + " fue deshabilitada";
        };
    }

    /**
     * Cuerpo HTML inline: título/venue/fecha del evento (o la razón de
     * deshabilitación) + enlace al evento cuando lo hay.
     */
    public String htmlBody(Notification notification) {
        if (notification.getType() == NotificationType.PROVIDER_DISABLED) {
            return providerDisabledBody(notification);
        }
        return eventBody(notification.getEvent());
    }

    private String providerDisabledBody(Notification notification) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>La fuente <strong>").append(notification.getProviderName())
                .append("</strong> fue deshabilitada.</p>");
        if (notification.getDisabledReason() != null && !notification.getDisabledReason().isBlank()) {
            sb.append("<p>Motivo: ").append(notification.getDisabledReason()).append("</p>");
        }
        return sb.toString();
    }

    private String eventBody(Event event) {
        if (event == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (event.getTitle() != null) {
            sb.append("<p><strong>").append(event.getTitle()).append("</strong></p>");
        }
        if (event.getVenue() != null) {
            sb.append("<p>Lugar: ").append(event.getVenue()).append("</p>");
        }
        String dateText = event.getEventDate() != null ? event.getEventDate().toString() : event.getEventDateRaw();
        if (dateText != null) {
            sb.append("<p>Fecha: ").append(dateText).append("</p>");
        }
        if (event.getExternalId() != null) {
            sb.append("<p><a href=\"").append(EVENT_LINK_BASE).append(event.getExternalId())
                    .append("\">Ver evento</a></p>");
        }
        return sb.toString();
    }

    private String searchTerm(Notification notification) {
        Search search = notification.getSearch();
        return search == null ? null : search.getTerm();
    }
}
