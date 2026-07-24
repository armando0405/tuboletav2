package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.AdminUserResponse;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.domain.enums.UserRole;
import com.tuboleta.backend.domain.enums.UserStatus;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.AdminUserService;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.GenericException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> list() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(AdminUserServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponse setStatus(Long actingUserId, Long targetUserId, boolean active) {
        User user = requireOther(actingUserId, targetUserId);
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserResponse setRole(Long actingUserId, Long targetUserId, UserRole role) {
        User user = requireOther(actingUserId, targetUserId);
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    /**
     * Carga el usuario objetivo, impidiendo que el admin se modifique a sí
     * mismo (evita auto-bloqueo / auto-degradación).
     */
    private User requireOther(Long actingUserId, Long targetUserId) {
        if (targetUserId.equals(actingUserId)) {
            throw new GenericException(HttpStatus.BAD_REQUEST, ErrorMessage.CANNOT_MODIFY_SELF);
        }
        return userRepository.findById(targetUserId)
                .orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND));
    }

    private static AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getEmail(), user.getName(),
                user.getRole(), user.getStatus(), user.getCreatedAt());
    }
}
