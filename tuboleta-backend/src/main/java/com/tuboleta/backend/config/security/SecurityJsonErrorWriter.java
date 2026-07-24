package com.tuboleta.backend.config.security;

import com.tuboleta.backend.utils.constants.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Escribe el envelope de error ({@code ObjectResponse}, code -1) a mano,
 * evitando depender del {@code ObjectMapper} autoconfigurado (Jackson 3 en
 * Spring Boot 4 — ver nota en {@code MonitoringDispatcher}) desde puntos que
 * corren FUERA de la cadena normal de {@code HttpMessageConverter}
 * ({@code AuthenticationEntryPoint}/{@code AccessDeniedHandler}). El mensaje
 * siempre es uno de los literales fijos de {@code ErrorMessage}, así que el
 * escape es trivial.
 */
final class SecurityJsonErrorWriter {

    private SecurityJsonErrorWriter() {
    }

    static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":" + ErrorCode.ERROR + ",\"msg\":\"" + escaped + "\"}");
    }
}
