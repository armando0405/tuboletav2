package com.tuboleta.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.api.dtos.AdminUserResponse;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.domain.enums.UserRole;
import com.tuboleta.backend.domain.enums.UserStatus;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.utils.exception.GenericException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserRepository userRepository;
    private AdminUserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminUserServiceImpl(userRepository);
    }

    private User target() {
        return User.builder().id(20L).email("u@x.com").name("Usuario")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
    }

    @Test
    void setStatus_onSelf_throwsCannotModifySelf() {
        assertThatThrownBy(() -> service.setStatus(1L, 1L, false))
                .isInstanceOf(GenericException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setRole_onSelf_throwsCannotModifySelf() {
        assertThatThrownBy(() -> service.setRole(1L, 1L, UserRole.USER))
                .isInstanceOf(GenericException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setStatus_onOther_deactivates() {
        User u = target();
        when(userRepository.findById(20L)).thenReturn(Optional.of(u));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse resp = service.setStatus(1L, 20L, false);

        assertThat(u.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(resp.status()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void setRole_onOther_promotesToAdmin() {
        User u = target();
        when(userRepository.findById(20L)).thenReturn(Optional.of(u));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse resp = service.setRole(1L, 20L, UserRole.ADMIN);

        assertThat(u.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(resp.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void setStatus_targetNotFound_throws() {
        when(userRepository.findById(20L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.setStatus(1L, 20L, true))
                .isInstanceOf(GenericException.class);
    }
}
