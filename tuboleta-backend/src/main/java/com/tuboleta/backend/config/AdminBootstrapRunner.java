package com.tuboleta.backend.config;

import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.domain.enums.UserRole;
import com.tuboleta.backend.domain.enums.UserStatus;
import com.tuboleta.backend.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el ADMIN inicial (REQ-USU-001) si {@code users} está vacía, para que
 * el sistema arranque con al menos un usuario capaz de gestionar fuentes.
 * Solo corre esa primera vez: en arranques posteriores la tabla ya no está
 * vacía y este runner no hace nada.
 */
@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LogManager.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${ADMIN_EMAIL:admin@tuboleta.local}") String adminEmail,
                                 @Value("${ADMIN_PASSWORD:admin123}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = User.builder()
                .email(adminEmail)
                .name("Administrador")
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);
        log.info("Tabla 'users' vacia: se creo el usuario ADMIN inicial con email '{}'", adminEmail);
    }
}
