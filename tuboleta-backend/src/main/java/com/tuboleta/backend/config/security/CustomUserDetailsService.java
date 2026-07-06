package com.tuboleta.backend.config.security;

import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Resuelve el email a un {@link AppUserPrincipal} (REQ-USU-002). No filtra
 * el estado INACTIVE aquí: lo hace {@code AbstractUserDetailsAuthenticationProvider}
 * a partir de {@code UserDetails#isEnabled()}, lanzando {@code DisabledException}
 * (subtipo de {@code AuthenticationException}, mapeado a 401 genérico).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        return new AppUserPrincipal(user);
    }
}
