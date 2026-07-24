package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.DestinationRequest;
import com.tuboleta.backend.api.dtos.DestinationResponse;
import com.tuboleta.backend.config.security.AppUserPrincipal;
import com.tuboleta.backend.service.DestinationService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import com.tuboleta.backend.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Destinos de notificación propios (REQ-NOT-001). Sin delete físico: solo
 * activar/desactivar.
 */
@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public ObjectListResponse<DestinationResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                destinationService.listMine(principal.getId()));
    }

    @PostMapping
    public ObjectResponse<DestinationResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
                                                        @Valid @RequestBody DestinationRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                destinationService.create(principal.getId(), request));
    }

    @PatchMapping("/{id}/toggle")
    public ObjectResponse<DestinationResponse> toggle(@AuthenticationPrincipal AppUserPrincipal principal,
                                                        @PathVariable Long id) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                destinationService.toggle(principal.getId(), id));
    }
}
