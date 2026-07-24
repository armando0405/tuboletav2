package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.FrequencyRequest;
import com.tuboleta.backend.api.dtos.FrequencyResponse;
import com.tuboleta.backend.service.FrequencyService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import com.tuboleta.backend.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión del catálogo de frecuencias (panel admin, REQ-BUS-005). Bajo
 * {@code /api/admin/**} → solo ADMIN (SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/frequencies")
public class AdminFrequencyController {

    private final FrequencyService frequencyService;

    public AdminFrequencyController(FrequencyService frequencyService) {
        this.frequencyService = frequencyService;
    }

    @GetMapping
    public ObjectListResponse<FrequencyResponse> listAll() {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, frequencyService.listAll());
    }

    @PostMapping
    public ObjectResponse<FrequencyResponse> create(@Valid @RequestBody FrequencyRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, frequencyService.create(request));
    }

    @PutMapping("/{id}")
    public ObjectResponse<FrequencyResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody FrequencyRequest request) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, frequencyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ObjectResponse<Void> delete(@PathVariable Long id) {
        frequencyService.delete(id);
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS);
    }
}
