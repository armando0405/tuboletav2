package com.tuboleta.backend.service.extraction;

/**
 * Datos crudos de un evento extraídos de un proveedor, antes de cualquier
 * filtrado por término (REQ-BUS-003) o persistencia.
 *
 * @param externalId   identificador estable del evento en el proveedor
 *                      (para TuBoleta: el {@code href} del enlace del evento)
 * @param title         título del evento
 * @param venue         nombre del lugar/venue; puede ser {@code null}
 * @param city          ciudad del evento; puede ser {@code null}
 * @param eventDateRaw  fecha del evento tal como la publica el proveedor
 *                      (p.ej. "5 Jul" o "21 Mar - 9 May"); puede ser {@code null}
 * @param rawJson       payload completo extraído en JSON, para auditoría
 *                      (persistido en {@code events.raw_json})
 */
public record RawEventData(
        String externalId,
        String title,
        String venue,
        String city,
        String eventDateRaw,
        String rawJson
) {
}
