package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.ProviderAdminResponse;
import com.tuboleta.backend.api.dtos.ProviderCreateRequest;
import com.tuboleta.backend.api.dtos.ProviderUpdateRequest;
import java.util.List;

/**
 * Administración de fuentes (REQ-FUE-001/002), solo ADMIN (aplicado por
 * {@code SecurityConfig} sobre {@code /api/admin/**}).
 */
public interface AdminProviderService {

    List<ProviderAdminResponse> list();

    ProviderAdminResponse create(ProviderCreateRequest request);

    ProviderAdminResponse update(Long providerId, ProviderUpdateRequest request);

    ProviderAdminResponse disable(Long providerId, String reason);

    ProviderAdminResponse enable(Long providerId);
}
