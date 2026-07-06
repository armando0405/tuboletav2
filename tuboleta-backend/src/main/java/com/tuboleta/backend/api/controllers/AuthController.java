package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.LoginRequest;
import com.tuboleta.backend.api.dtos.RegisterRequest;
import com.tuboleta.backend.api.dtos.UserResponse;
import com.tuboleta.backend.config.security.AppUserPrincipal;
import com.tuboleta.backend.service.AuthService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.GenericException;
import com.tuboleta.backend.utils.response.ObjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro/login/logout/me (REQ-USU-001/002). El login es manual (sin
 * {@code formLogin}): autentica contra el {@code AuthenticationManager} y
 * persiste el contexto en la sesión HTTP a mano, porque el frontend es un
 * SPA que llama por AJAX (axios {@code withCredentials: true}), no un
 * formulario.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService,
                           AuthenticationManager authenticationManager,
                           SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ObjectResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, user);
    }

    @PostMapping("/login")
    public ObjectResponse<UserResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        Authentication authResult;
        try {
            authResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new GenericException(HttpStatus.UNAUTHORIZED, ErrorMessage.INVALID_CREDENTIALS);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        AppUserPrincipal principal = (AppUserPrincipal) authResult.getPrincipal();
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, toResponse(principal));
    }

    @PostMapping("/logout")
    public ObjectResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
        SecurityContextHolder.clearContext();
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS);
    }

    @GetMapping("/me")
    public ObjectResponse<UserResponse> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, toResponse(principal));
    }

    private static UserResponse toResponse(AppUserPrincipal principal) {
        return new UserResponse(principal.getId(), principal.getUsername(), principal.getName(), principal.getRole());
    }
}
