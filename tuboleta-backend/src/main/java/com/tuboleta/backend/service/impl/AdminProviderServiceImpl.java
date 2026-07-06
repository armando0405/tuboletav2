package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.ProviderAdminResponse;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderStatus;
import com.tuboleta.backend.repository.ProviderRepository;
import com.tuboleta.backend.service.AdminProviderService;
import com.tuboleta.backend.service.NotificationService;
import com.tuboleta.backend.utils.exception.NotFoundRegisterException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deshabilitar/reactivar una fuente (REQ-FUE-002): actualiza
 * {@code providers.status/status_reason/status_changed_at} y delega el
 * fan-out de la notificación PROVIDER_DISABLED en {@link NotificationService}
 * (ya implementado en Task 6).
 */
@Service
public class AdminProviderServiceImpl implements AdminProviderService {

    private final ProviderRepository providerRepository;
    private final NotificationService notificationService;

    public AdminProviderServiceImpl(ProviderRepository providerRepository, NotificationService notificationService) {
        this.providerRepository = providerRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderAdminResponse> list() {
        return providerRepository.findAll().stream().map(AdminProviderServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public ProviderAdminResponse disable(Long providerId, String reason) {
        Provider provider = findProvider(providerId);
        provider.setStatus(ProviderStatus.DISABLED);
        provider.setStatusReason(reason);
        provider.setStatusChangedAt(Instant.now());
        provider = providerRepository.save(provider);
        notificationService.notifyProviderDisabled(provider, reason);
        return toResponse(provider);
    }

    @Override
    @Transactional
    public ProviderAdminResponse enable(Long providerId) {
        Provider provider = findProvider(providerId);
        provider.setStatus(ProviderStatus.ACTIVE);
        provider.setStatusReason(null);
        provider.setStatusChangedAt(Instant.now());
        provider = providerRepository.save(provider);
        return toResponse(provider);
    }

    private Provider findProvider(Long providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new NotFoundRegisterException("Proveedor", "id", providerId));
    }

    private static ProviderAdminResponse toResponse(Provider provider) {
        return new ProviderAdminResponse(
                provider.getId(),
                provider.getName(),
                provider.getProviderType(),
                provider.getBaseUrl(),
                provider.getStatus(),
                provider.getStatusReason(),
                provider.getStatusChangedAt());
    }
}
