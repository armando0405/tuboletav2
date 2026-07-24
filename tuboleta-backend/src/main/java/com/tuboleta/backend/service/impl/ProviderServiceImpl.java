package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.ProviderResponse;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderStatus;
import com.tuboleta.backend.repository.ProviderRepository;
import com.tuboleta.backend.service.ProviderService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderServiceImpl(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> listActive() {
        return providerRepository.findByStatus(ProviderStatus.ACTIVE).stream()
                .map(ProviderServiceImpl::toResponse)
                .toList();
    }

    private static ProviderResponse toResponse(Provider provider) {
        return new ProviderResponse(
                provider.getId(),
                provider.getName(),
                provider.getProviderType());
    }
}
