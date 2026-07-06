package com.tuboleta.backend.api.dtos;

import com.tuboleta.backend.utils.constants.ErrorMessage;
import jakarta.validation.constraints.AssertTrue;
import java.util.List;
import java.util.Set;

/**
 * Edición parcial de una búsqueda (REQ-BUS-005): ambos campos son
 * opcionales — {@code null} significa "no tocar este campo".
 * {@code destinationIds} reemplaza el conjunto completo de destinos activos.
 */
public record SearchUpdateRequest(
        Integer checkFrequencyHours,
        List<Long> destinationIds
) {

    private static final Set<Integer> VALID_FREQUENCIES = Set.of(6, 12, 24, 48);

    @AssertTrue(message = ErrorMessage.INVALID_FREQUENCY)
    public boolean isCheckFrequencyValid() {
        return checkFrequencyHours == null || VALID_FREQUENCIES.contains(checkFrequencyHours);
    }
}
