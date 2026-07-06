package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.ProviderAdminResponse;
import java.util.List;

/**
 * Administración de fuentes (REQ-FUE-001/002), solo ADMIN (aplicado por
 * {@code SecurityConfig} sobre {@code /api/admin/**}).
 */
public interface AdminProviderService {

    List<ProviderAdminResponse> list();

    ProviderAdminResponse disable(Long providerId, String reason);

    ProviderAdminResponse enable(Long providerId);
}
