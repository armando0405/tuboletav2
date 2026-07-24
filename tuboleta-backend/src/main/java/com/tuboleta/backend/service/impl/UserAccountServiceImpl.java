package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.ChangePasswordRequest;
import com.tuboleta.backend.api.dtos.ForgotPasswordRequest;
import com.tuboleta.backend.api.dtos.ResetPasswordRequest;
import com.tuboleta.backend.api.dtos.UpdateProfileRequest;
import com.tuboleta.backend.api.dtos.UserResponse;
import com.tuboleta.backend.domain.entities.PasswordResetToken;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.repository.PasswordResetTokenRepository;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.UserAccountService;
import com.tuboleta.backend.service.notification.EmailService;
import com.tuboleta.backend.service.notification.PasswordResetEmailBuilder;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.GenericException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final Logger log = LogManager.getLogger(UserAccountServiceImpl.class);
    private static final Duration TOKEN_TTL = Duration.ofHours(1);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetEmailBuilder emailBuilder;
    private final String publicUrl;

    public UserAccountServiceImpl(UserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService,
                                   PasswordResetEmailBuilder emailBuilder,
                                   @org.springframework.beans.factory.annotation.Value(
                                           "${app.public-url:http://localhost:7075}") String publicUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailBuilder = emailBuilder;
        this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new GenericException(HttpStatus.BAD_REQUEST, ErrorMessage.CURRENT_PASSWORD_INVALID);
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND));
        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            userRepository.findByEmail(request.email()).ifPresent(other -> {
                if (!other.getId().equals(userId)) {
                    throw new GenericException(HttpStatus.CONFLICT,
                            ErrorMessage.EMAIL_ALREADY_REGISTERED, request.email());
                }
            });
            user.setEmail(request.email());
        }
        user.setName(request.name());
        user = userRepository.save(user);
        return AuthServiceImpl.toResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Anti-enumeración: si el correo no existe, no se hace nada y se
        // devuelve éxito igual (el controlador nunca revela la diferencia).
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            tokenRepository.deleteByUserId(user.getId());
            String rawToken = generateRawToken();
            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(sha256Hex(rawToken))
                    .expiresAt(Instant.now().plus(TOKEN_TTL))
                    .build();
            tokenRepository.save(token);

            String resetUrl = publicUrl + "/reset-password?token=" + rawToken;
            emailService.sendHtml(user.getEmail(), emailBuilder.subject(), emailBuilder.html(user.getName(), resetUrl));
            log.info("Recuperación: token emitido para el usuario id={} (correo enviado)", user.getId());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256Hex(request.token()))
                .orElseThrow(() -> new GenericException(HttpStatus.BAD_REQUEST, ErrorMessage.RESET_LINK_INVALID));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new GenericException(HttpStatus.BAD_REQUEST, ErrorMessage.RESET_LINK_INVALID);
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
        log.info("Recuperación: contraseña restablecida para el usuario id={}", user.getId());
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre está disponible en la JVM.
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
