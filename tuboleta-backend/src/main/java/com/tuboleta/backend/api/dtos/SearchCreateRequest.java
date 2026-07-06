package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.utils.constants.ErrorMessage;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Alta de una búsqueda (REQ-BUS-001/002/005): el término se normaliza en el
 * servicio ({@code TermNormalizer}), la frecuencia debe ser uno de los
 * presets cerrados y al menos un proveedor es obligatorio (destinos no).
 */
public record SearchCreateRequest(
        @NotBlank String term,
        @NotNull Integer checkFrequencyHours,
        @NotEmpty List<Long> providerIds,
        List<Long> destinationIds
) {

    private static final Set<Integer> VALID_FREQUENCIES = Set.of(6, 12, 24, 48);

    @AssertTrue(message = ErrorMessage.INVALID_FREQUENCY)
    public boolean isCheckFrequencyValid() {
        return checkFrequencyHours == null || VALID_FREQUENCIES.contains(checkFrequencyHours);
    }
}
