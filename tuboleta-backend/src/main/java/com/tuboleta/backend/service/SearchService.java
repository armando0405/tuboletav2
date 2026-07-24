package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.EventChangeResponse;
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

    List<EventChangeResponse> eventChanges(Long userId, Long searchId, Long eventId);

    SearchResponse update(Long userId, Long searchId, SearchUpdateRequest request);

    SearchResponse togglePair(Long userId, Long searchId, Long providerId);

    /**
     * Pausa/reanuda la búsqueda COMPLETA (todos sus proveedores a la vez),
     * alternando {@code status} ACTIVE↔INACTIVE (REQ-FE-001). Nunca toca
     * DELETED: si la búsqueda está eliminada lógicamente se trata como
     * inexistente.
     */
    SearchResponse toggleStatus(Long userId, Long searchId);

    /**
     * Dispara AHORA una corrida de monitoreo de la búsqueda, sin esperar su
     * ventana de frecuencia (botón "ejecutar ahora"). Corre el mismo camino
     * que el scheduler (extracción → detección → notificación → registro)
     * para cada proveedor activo del par y devuelve el total de eventos de la
     * búsqueda tras la corrida, para dar feedback inmediato al usuario.
     */
    long runNow(Long userId, Long searchId);

    void delete(Long userId, Long searchId);
}
