package com.tuboleta.backend.service.notification;

import org.springframework.stereotype.Component;

/**
 * Arma el asunto y el HTML del correo de recuperación de contraseña, con la
 * misma estética dark del resto de correos (REQ-NOT-002). Sin estado.
 */
@Component
public class PasswordResetEmailBuilder {

    private static final String BG = "#0A0A0C";
    private static final String CARD = "#15161C";
    private static final String BORDER = "#2A2E3F";
    private static final String TEXT = "#E0E0E6";
    private static final String MUTED = "#9CA3AF";
    private static final String INDIGO = "#5E6AD2";

    public String subject() {
        return "[TuBoleta] Recupera tu contraseña";
    }

    public String html(String name, String resetUrl) {
        String safeName = escape(name == null ? "" : name);
        String safeUrl = escape(resetUrl);
        return "<div style=\"background:" + BG + ";padding:24px;font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:520px;margin:0 auto;background:" + CARD + ";border-radius:14px;"
                + "overflow:hidden;border:1px solid " + BORDER + ";\">"
                + "<tr><td style=\"height:6px;background:" + INDIGO + ";\"></td></tr>"
                + "<tr><td style=\"padding:26px 28px 22px;\">"
                + "<h1 style=\"color:" + TEXT + ";font-size:19px;font-weight:bold;margin:0 0 14px;\">"
                + "Recupera tu contraseña</h1>"
                + "<p style=\"color:" + TEXT + ";font-size:14px;margin:0 0 8px;\">Hola " + safeName + ",</p>"
                + "<p style=\"color:" + MUTED + ";font-size:14px;margin:0 0 22px;line-height:1.5;\">"
                + "Recibimos una solicitud para restablecer tu contraseña. Haz clic en el botón "
                + "(el enlace vence en 1 hora). Si no fuiste tú, ignora este correo.</p>"
                + "<div><a href=\"" + safeUrl + "\" style=\"display:inline-block;background:" + INDIGO + ";"
                + "color:#fff;text-decoration:none;font-size:14px;font-weight:bold;padding:11px 22px;"
                + "border-radius:10px;\">Cambiar mi contraseña</a></div>"
                + "<p style=\"color:" + MUTED + ";font-size:12px;margin:22px 0 0;word-break:break-all;\">"
                + "O copia este enlace: " + safeUrl + "</p>"
                + "</td></tr>"
                + "<tr><td style=\"padding:14px 28px;border-top:1px solid " + BORDER + ";color:#6B7280;"
                + "font-size:12px;\">TuBoleta · seguridad de la cuenta</td></tr>"
                + "</table></div>";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
