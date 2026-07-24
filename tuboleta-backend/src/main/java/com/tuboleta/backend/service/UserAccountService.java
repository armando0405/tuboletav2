package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.ChangePasswordRequest;
import com.tuboleta.backend.api.dtos.ForgotPasswordRequest;
import com.tuboleta.backend.api.dtos.ResetPasswordRequest;
import com.tuboleta.backend.api.dtos.UpdateProfileRequest;
import com.tuboleta.backend.api.dtos.UserResponse;

/**
 * Autoservicio de cuenta (Fase 1): cambiar contraseña, editar perfil y el flujo
 * de recuperación de contraseña por correo.
 */
public interface UserAccountService {

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * Emite un token de recuperación y envía el correo. No revela si el correo
     * existe: siempre termina sin error (anti-enumeración de usuarios).
     */
    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
