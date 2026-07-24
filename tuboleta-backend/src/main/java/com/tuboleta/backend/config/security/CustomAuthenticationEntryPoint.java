package com.tuboleta.backend.config.security;

import com.tuboleta.backend.utils.constants.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Sin sesión válida en un recurso protegido → JSON del envelope (nunca la
 * página de login por defecto de Spring Security).
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        SecurityJsonErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorMessage.AUTH_REQUIRED);
    }
}
