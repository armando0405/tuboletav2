package com.tuboleta.backend.service.notification;

import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.enums.NotificationType;

/**
 * Genera el asunto y el cuerpo HTML del correo de notificación (REQ-NOT-002),
 * en español y sin motor de plantillas. El HTML usa estilos INLINE (los
 * clientes de correo ignoran {@code <style>}/CSS externo) con una estética dark
 * y un acento de color según el tipo: NEW verde, CHANGED ámbar, REMOVED y
 * PROVIDER_DISABLED rojo. Sin estado: {@code new EmailContentBuilder()}.
 */
public class EmailContentBuilder {

    private static final String SUBJECT_PREFIX = "[TuBoleta] ";
    private static final String EVENT_LINK_BASE = "https://www.tuboleta.com";

    // Paleta "Dark Operations" (coherente con el frontend).
    private static final String BG = "#0A0A0C";
    private static final String CARD = "#15161C";
    private static final String BORDER = "#2A2E3F";
    private static final String TEXT = "#E0E0E6";
    private static final String MUTED = "#9CA3AF";
    private static final String INDIGO = "#5E6AD2";

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

    public String htmlBody(Notification notification) {
        Accent accent = accentFor(notification.getType());
        String inner = notification.getType() == NotificationType.PROVIDER_DISABLED
                ? providerDisabledInner(notification)
                : eventInner(notification);
        return shell(accent, notification, inner);
    }

    // ---- estructura general (card dark + barra/insignia de acento) ----

    private String shell(Accent accent, Notification notification, String inner) {
        String term = escape(searchTerm(notification));
        return "<div style=\"background:" + BG + ";padding:24px;"
                + "font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:520px;margin:0 auto;background:" + CARD + ";border-radius:14px;"
                + "overflow:hidden;border:1px solid " + BORDER + ";\">"
                + "<tr><td style=\"height:6px;background:" + accent.color + ";\"></td></tr>"
                + "<tr><td style=\"padding:26px 28px 22px;\">"
                + "<span style=\"display:inline-block;background:" + accent.bg + ";color:" + accent.color + ";"
                + "font-size:11px;font-weight:bold;letter-spacing:.6px;text-transform:uppercase;"
                + "padding:5px 12px;border-radius:999px;\">" + accent.label + "</span>"
                + "<p style=\"color:" + MUTED + ";font-size:13px;margin:16px 0 0;\">Para tu búsqueda</p>"
                + "<h1 style=\"color:" + TEXT + ";font-size:19px;font-weight:bold;margin:2px 0 20px;\">«"
                + term + "»</h1>"
                + inner
                + "</td></tr>"
                + "<tr><td style=\"padding:14px 28px;border-top:1px solid " + BORDER + ";color:#6B7280;"
                + "font-size:12px;\">TuBoleta · monitoreo automático de eventos</td></tr>"
                + "</table></div>";
    }

    private String eventInner(Notification notification) {
        Event event = notification.getEvent();
        if (event == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (event.getTitle() != null) {
            sb.append("<div style=\"color:").append(TEXT).append(";font-size:17px;font-weight:bold;")
                    .append("margin-bottom:12px;\">").append(escape(event.getTitle())).append("</div>");
        }
        sb.append(detailRow("Lugar", event.getVenue()));
        String dateText = event.getEventDate() != null ? event.getEventDate().toString() : event.getEventDateRaw();
        sb.append(detailRow("Fecha", dateText));
        if (event.getExternalId() != null) {
            String href = EVENT_LINK_BASE + event.getExternalId();
            sb.append("<div style=\"margin-top:22px;\">")
                    .append("<a href=\"").append(escape(href)).append("\" ")
                    .append("style=\"display:inline-block;background:").append(INDIGO).append(";color:#fff;")
                    .append("text-decoration:none;font-size:14px;font-weight:bold;padding:11px 22px;")
                    .append("border-radius:10px;\">Ver evento en TuBoleta</a></div>");
        }
        return sb.toString();
    }

    private String providerDisabledInner(Notification notification) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"color:").append(TEXT).append(";font-size:15px;margin-bottom:10px;\">")
                .append("La fuente <strong>").append(escape(notification.getProviderName()))
                .append("</strong> fue deshabilitada, así que dejará de monitorearse temporalmente.</div>");
        if (notification.getDisabledReason() != null && !notification.getDisabledReason().isBlank()) {
            sb.append(detailRow("Motivo", notification.getDisabledReason()));
        }
        return sb.toString();
    }

    private String detailRow(String label, String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:8px;\">"
                + "<tr>"
                + "<td style=\"color:" + MUTED + ";font-size:13px;padding-right:10px;white-space:nowrap;\">"
                + escape(label) + "</td>"
                + "<td style=\"color:" + TEXT + ";font-size:13px;\">" + escape(value) + "</td>"
                + "</tr></table>";
    }

    private Accent accentFor(NotificationType type) {
        return switch (type) {
            case NEW -> new Accent("#22C55E", "#132A1D", "Nuevo evento");
            case CHANGED -> new Accent("#F59E0B", "#2E2410", "Evento actualizado");
            case REMOVED -> new Accent("#EF4444", "#2E1417", "Ya no disponible");
            case PROVIDER_DISABLED -> new Accent("#EF4444", "#2E1417", "Fuente deshabilitada");
        };
    }

    private String searchTerm(Notification notification) {
        Search search = notification.getSearch();
        return search == null ? "" : search.getTerm();
    }

    /** Escape mínimo de HTML para no romper el markup con datos del scraper. */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** Trío de acento (color de barra/insignia, fondo de la insignia, etiqueta). */
    private record Accent(String color, String bg, String label) {
    }
}
