package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.EventResponse;
import com.tuboleta.backend.api.dtos.SearchCreateRequest;
import com.tuboleta.backend.api.dtos.SearchResponse;
import com.tuboleta.backend.api.dtos.SearchUpdateRequest;
import com.tuboleta.backend.config.security.AppUserPrincipal;
import com.tuboleta.backend.service.SearchService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import com.tuboleta.backend.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Búsquedas propias del usuario (REQ-BUS-001/002/004/005). Ownership: los
 * recursos ajenos se tratan como inexistentes (404), nunca se filtra su
 * existencia a otro usuario.
 */
@RestController
@RequestMapping("/api/searches")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ObjectResponse<SearchResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
                                                  @Valid @RequestBody SearchCreateRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                searchService.create(principal.getId(), request));
    }

    @GetMapping
    public ObjectListResponse<SearchResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                searchService.listMine(principal.getId()));
    }

    @GetMapping("/{id}/events")
    public ObjectListResponse<EventResponse> events(@AuthenticationPrincipal AppUserPrincipal principal,
                                                     @PathVariable Long id) {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                searchService.events(principal.getId(), id));
    }

    @PatchMapping("/{id}")
    public ObjectResponse<SearchResponse> update(@AuthenticationPrincipal AppUserPrincipal principal,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody SearchUpdateRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                searchService.update(principal.getId(), id, request));
    }

    @PatchMapping("/{id}/providers/{providerId}/toggle")
    public ObjectResponse<SearchResponse> togglePair(@AuthenticationPrincipal AppUserPrincipal principal,
                                                      @PathVariable Long id,
                                                      @PathVariable Long providerId) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                searchService.togglePair(principal.getId(), id, providerId));
    }

    @DeleteMapping("/{id}")
    public ObjectResponse<Void> delete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        searchService.delete(principal.getId(), id);
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS);
    }
}
