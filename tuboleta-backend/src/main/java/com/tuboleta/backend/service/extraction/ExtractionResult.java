package com.tuboleta.backend.service.extraction;

import java.util.List;

/**
 * Resultado de una corrida de extracción contra un proveedor.
 *
 * <p>{@code failedItems} cuenta los ítems (p.ej. un {@code article} del HTML)
 * que lanzaron error al extraerse individualmente; el resto de ítems se
 * devuelve igual en {@code items} — una corrida parcial (REQ-DET-005) no
 * descarta lo que sí se pudo extraer.</p>
 *
 * @param items       eventos extraídos exitosamente
 * @param failedItems cantidad de ítems que fallaron al extraerse
 */
public record ExtractionResult(List<RawEventData> items, int failedItems) {
}
