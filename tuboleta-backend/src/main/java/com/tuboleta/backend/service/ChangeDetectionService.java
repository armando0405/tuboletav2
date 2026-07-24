package com.tuboleta.backend.service;

import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.service.detection.DetectedChange;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import java.util.List;

/**
 * Detección de cambios (REQ-DET-001/002/003 + filtro REQ-BUS-003): dado un
 * par búsqueda↔proveedor y el resultado de una extracción, decide qué
 * eventos son NEW/CHANGED/REMOVED, persiste {@code events}/{@code event_changes}
 * y devuelve los hechos notificables. NO envía notificaciones (REQ-ARQ-004).
 */
public interface ChangeDetectionService {

    /**
     * Procesa el resultado de una corrida de extracción contra un par
     * búsqueda↔proveedor y actualiza el histórico de eventos.
     *
     * @param pair       par búsqueda↔proveedor sobre el que corrió la extracción
     * @param extraction resultado de la extracción (ítems + cantidad de fallidos);
     *                   la corrida se considera completamente exitosa cuando
     *                   {@code extraction.failedItems() == 0} (REQ-DET-005)
     * @return los cambios notificables detectados en esta corrida (NEW/CHANGED/REMOVED)
     */
    List<DetectedChange> detect(SearchProvider pair, ExtractionResult extraction);
}
