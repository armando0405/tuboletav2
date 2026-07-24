package com.tuboleta.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.api.dtos.ChangePasswordRequest;
import com.tuboleta.backend.api.dtos.ForgotPasswordRequest;
import com.tuboleta.backend.api.dtos.ResetPasswordRequest;
import com.tuboleta.backend.api.dtos.UpdateProfileRequest;
import com.tuboleta.backend.domain.entities.PasswordResetToken;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.repository.PasswordResetTokenRepository;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.notification.EmailService;
import com.tuboleta.backend.service.notification.PasswordResetEmailBuilder;
import com.tuboleta.backend.utils.exception.GenericException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private PasswordResetEmailBuilder emailBuilder;

    private UserAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAccountServiceImpl(userRepository, tokenRepository, passwordEncoder,
                emailService, emailBuilder, "http://localhost:7075/");
    }

    private User user() {
        return User.builder().id(5L).email("ana@x.com").name("Ana").passwordHash("HASH_ACTUAL").build();
    }

    // ---------- cambiar contraseña ----------

    @Test
    void changePassword_wrongCurrent_throwsAndDoesNotSave() {
        User u = user();
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", "HASH_ACTUAL")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(5L, new ChangePasswordRequest("mala", "nuevaClave1")))
                .isInstanceOf(GenericException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_correctCurrent_encodesAndSaves() {
        User u = user();
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("actual123", "HASH_ACTUAL")).thenReturn(true);
        when(passwordEncoder.encode("nuevaClave1")).thenReturn("HASH_NUEVO");

        service.changePassword(5L, new ChangePasswordRequest("actual123", "nuevaClave1"));

        assertThat(u.getPasswordHash()).isEqualTo("HASH_NUEVO");
        verify(userRepository).save(u);
    }

    // ---------- editar perfil ----------

    @Test
    void updateProfile_emailTakenByAnother_throwsConflict() {
        User u = user();
        User other = User.builder().id(99L).email("nuevo@x.com").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(userRepository.findByEmail("nuevo@x.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.updateProfile(5L, new UpdateProfileRequest("Ana R", "nuevo@x.com")))
                .isInstanceOf(GenericException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_sameEmail_updatesNameOnly() {
        User u = user();
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateProfile(5L, new UpdateProfileRequest("Ana Ramirez", "ana@x.com"));

        assertThat(u.getName()).isEqualTo("Ana Ramirez");
        assertThat(u.getEmail()).isEqualTo("ana@x.com");
        verify(userRepository, never()).findByEmail(anyString()); // no chequea duplicado si no cambió
    }

    // ---------- recuperar contraseña ----------

    @Test
    void forgotPassword_unknownEmail_silentNoTokenNoMail() {
        when(userRepository.findByEmail("nadie@x.com")).thenReturn(Optional.empty());

        service.forgotPassword(new ForgotPasswordRequest("nadie@x.com"));

        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void forgotPassword_knownEmail_storesHashedTokenAndSendsMail() {
        User u = user();
        when(userRepository.findByEmail("ana@x.com")).thenReturn(Optional.of(u));
        when(emailBuilder.subject()).thenReturn("Recupera tu contraseña");
        when(emailBuilder.html(eq("Ana"), anyString())).thenReturn("<html>");

        service.forgotPassword(new ForgotPasswordRequest("ana@x.com"));

        verify(tokenRepository).deleteByUserId(5L); // invalida los previos
        ArgumentCaptor<PasswordResetToken> tokenCap = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCap.capture());
        PasswordResetToken saved = tokenCap.getValue();
        assertThat(saved.getUser()).isEqualTo(u);
        assertThat(saved.getTokenHash()).hasSize(64); // SHA-256 hex, NO el token en claro
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());

        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        verify(emailBuilder).html(eq("Ana"), urlCap.capture());
        assertThat(urlCap.getValue()).startsWith("http://localhost:7075/reset-password?token=");
        verify(emailService).sendHtml(eq("ana@x.com"), eq("Recupera tu contraseña"), any());
    }

    // ---------- restablecer contraseña ----------

    @Test
    void resetPassword_unknownToken_throws() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("xxx", "nuevaClave1")))
                .isInstanceOf(GenericException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_expiredToken_throws() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user())
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("xxx", "nuevaClave1")))
                .isInstanceOf(GenericException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_usedToken_throws() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .usedAt(Instant.now())
                .build();
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("xxx", "nuevaClave1")))
                .isInstanceOf(GenericException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_validToken_setsPasswordAndMarksUsed() {
        User u = user();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(u)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nuevaClave1")).thenReturn("HASH_NUEVO");

        service.resetPassword(new ResetPasswordRequest("rawtoken", "nuevaClave1"));

        assertThat(u.getPasswordHash()).isEqualTo("HASH_NUEVO");
        assertThat(token.getUsedAt()).isNotNull();
        verify(userRepository).save(u);
        verify(tokenRepository).save(token);
    }
}
