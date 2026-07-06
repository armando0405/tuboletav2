package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.RegisterRequest;
import com.tuboleta.backend.api.dtos.UserResponse;

/**
 * Registro de usuarios autoservicio (REQ-USU-001/002). El login en sí vive en
 * {@code AuthController} porque necesita el {@code AuthenticationManager} y
 * la sesión HTTP directamente.
 */
public interface AuthService {

    /**
     * Crea un USER nuevo (rol fijo, nunca ADMIN por este camino).
     *
     * @param request datos de alta
     * @return el usuario creado, sin el hash de contraseña
     */
    UserResponse register(RegisterRequest request);
}
