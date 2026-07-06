package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.EventResponse;
import com.tuboleta.backend.api.dtos.SearchCreateRequest;
import com.tuboleta.backend.api.dtos.SearchResponse;
import com.tuboleta.backend.api.dtos.SearchUpdateRequest;
import java.util.List;

/**
 * Búsquedas propias del usuario (REQ-BUS-001/002/004/005). Ownership: un
 * recurso ajeno se trata como inexistente (nunca se filtra su existencia).
 */
public interface SearchService {

    SearchResponse create(Long userId, SearchCreateRequest request);

    List<SearchResponse> listMine(Long userId);

    List<EventResponse> events(Long userId, Long searchId);

    SearchResponse update(Long userId, Long searchId, SearchUpdateRequest request);

    SearchResponse togglePair(Long userId, Long searchId, Long providerId);

    void delete(Long userId, Long searchId);
}
