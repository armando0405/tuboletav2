package com.tuboleta.backend.config.security;

import com.tuboleta.backend.utils.constants.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Autenticado pero sin el rol requerido (ej. USER contra {@code /api/admin/**})
 * → JSON del envelope con 403, nunca la página de error por defecto.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        SecurityJsonErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN, ErrorMessage.ACCESS_DENIED);
    }
}
