package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.DestinationRequest;
import com.tuboleta.backend.api.dtos.DestinationResponse;
import java.util.List;

/**
 * Destinos de notificación reutilizables por usuario (REQ-NOT-001). Sin
 * delete físico: solo se activan/desactivan.
 */
public interface DestinationService {

    List<DestinationResponse> listMine(Long userId);

    DestinationResponse create(Long userId, DestinationRequest request);

    DestinationResponse toggle(Long userId, Long destinationId);
}
