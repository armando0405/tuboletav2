package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.DestinationRequest;
import com.tuboleta.backend.api.dtos.DestinationResponse;
import com.tuboleta.backend.domain.entities.NotifChannel;
import com.tuboleta.backend.domain.entities.UserNotificationChannel;
import com.tuboleta.backend.repository.NotifChannelRepository;
import com.tuboleta.backend.repository.UserNotificationChannelRepository;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.DestinationService;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.BusinessException;
import com.tuboleta.backend.utils.exception.GenericException;
import com.tuboleta.backend.utils.exception.NotFoundRegisterException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único canal soportado por ahora: EMAIL (REQ-NOT-002). Reutiliza la fila
 * existente de {@code notif_channels}, nunca crea el catálogo desde aquí.
 */
@Service
public class DestinationServiceImpl implements DestinationService {

    private static final String EMAIL_CHANNEL = "EMAIL";

    private final UserNotificationChannelRepository destinationRepository;
    private final NotifChannelRepository notifChannelRepository;
    private final UserRepository userRepository;

    public DestinationServiceImpl(UserNotificationChannelRepository destinationRepository,
                                   NotifChannelRepository notifChannelRepository,
                                   UserRepository userRepository) {
        this.destinationRepository = destinationRepository;
        this.notifChannelRepository = notifChannelRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationResponse> listMine(Long userId) {
        return destinationRepository.findByUserId(userId).stream()
                .map(DestinationServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DestinationResponse create(Long userId, DestinationRequest request) {
        NotifChannel channel = notifChannelRepository.findByName(EMAIL_CHANNEL)
                .orElseThrow(() -> new BusinessException(ErrorMessage.CHANNEL_NOT_FOUND, EMAIL_CHANNEL));
        if (!Boolean.TRUE.equals(channel.getIsActive())) {
            throw new GenericException(HttpStatus.CONFLICT, ErrorMessage.CHANNEL_INACTIVE, EMAIL_CHANNEL);
        }
        destinationRepository.findByUserIdAndChannelIdAndDestination(userId, channel.getId(), request.destination())
                .ifPresent(existing -> {
                    throw new GenericException(HttpStatus.CONFLICT, ErrorMessage.DESTINATION_DUPLICATE, request.destination());
                });

        UserNotificationChannel destination = UserNotificationChannel.builder()
                .user(userRepository.getReferenceById(userId))
                .channel(channel)
                .destination(request.destination())
                .isActive(true)
                .build();
        destination = destinationRepository.save(destination);
        return toResponse(destination);
    }

    @Override
    @Transactional
    public DestinationResponse toggle(Long userId, Long destinationId) {
        UserNotificationChannel destination = destinationRepository.findByIdAndUserId(destinationId, userId)
                .orElseThrow(() -> new NotFoundRegisterException("Destino", "id", destinationId));
        destination.setIsActive(!Boolean.TRUE.equals(destination.getIsActive()));
        destination = destinationRepository.save(destination);
        return toResponse(destination);
    }

    private static DestinationResponse toResponse(UserNotificationChannel destination) {
        return new DestinationResponse(
                destination.getId(),
                destination.getChannel().getName(),
                destination.getDestination(),
                destination.getIsActive());
    }
}
