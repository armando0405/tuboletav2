package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.FrequencyRequest;
import com.tuboleta.backend.api.dtos.FrequencyResponse;
import com.tuboleta.backend.domain.entities.Frequency;
import com.tuboleta.backend.repository.FrequencyRepository;
import com.tuboleta.backend.service.FrequencyService;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.GenericException;
import com.tuboleta.backend.utils.exception.NotFoundRegisterException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión del catálogo de frecuencias (REQ-BUS-005). El valor en minutos es
 * único; el admin puede activar/desactivar o eliminar. Borrar una frecuencia
 * NO afecta a las búsquedas existentes: cada búsqueda guarda su propio valor en
 * {@code check_frequency_minutes} (no hay FK), así que sigue corriendo a ese
 * intervalo aunque la opción ya no esté en el catálogo.
 */
@Service
public class FrequencyServiceImpl implements FrequencyService {

    private final FrequencyRepository frequencyRepository;

    public FrequencyServiceImpl(FrequencyRepository frequencyRepository) {
        this.frequencyRepository = frequencyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FrequencyResponse> listActive() {
        return frequencyRepository.findByIsActiveTrueOrderByMinutesAsc().stream()
                .map(FrequencyServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FrequencyResponse> listAll() {
        return frequencyRepository.findAllByOrderByMinutesAsc().stream()
                .map(FrequencyServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FrequencyResponse create(FrequencyRequest request) {
        frequencyRepository.findByMinutes(request.minutes()).ifPresent(existing -> {
            throw new GenericException(HttpStatus.CONFLICT, ErrorMessage.FREQUENCY_DUPLICATE, request.minutes());
        });
        Frequency frequency = Frequency.builder()
                .label(request.label())
                .minutes(request.minutes())
                .isActive(request.isActive() == null ? Boolean.TRUE : request.isActive())
                .build();
        return toResponse(frequencyRepository.save(frequency));
    }

    @Override
    @Transactional
    public FrequencyResponse update(Long id, FrequencyRequest request) {
        Frequency frequency = frequencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundRegisterException("Frecuencia", "id", id));
        frequencyRepository.findByMinutes(request.minutes())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new GenericException(HttpStatus.CONFLICT, ErrorMessage.FREQUENCY_DUPLICATE, request.minutes());
                });
        frequency.setLabel(request.label());
        frequency.setMinutes(request.minutes());
        if (request.isActive() != null) {
            frequency.setIsActive(request.isActive());
        }
        return toResponse(frequencyRepository.save(frequency));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Frequency frequency = frequencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundRegisterException("Frecuencia", "id", id));
        frequencyRepository.delete(frequency);
    }

    private static FrequencyResponse toResponse(Frequency frequency) {
        return new FrequencyResponse(
                frequency.getId(), frequency.getLabel(), frequency.getMinutes(), frequency.getIsActive());
    }
}
