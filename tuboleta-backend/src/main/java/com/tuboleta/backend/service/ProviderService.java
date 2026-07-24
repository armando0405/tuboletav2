package com.tuboleta.backend.service;

import com.tuboleta.backend.api.dtos.ProviderResponse;
import java.util.List;

/**
 * Catálogo público de fuentes (REQ-FUE-001), para cualquier usuario
 * autenticado al elegir proveedor(es) en una búsqueda. Solo ACTIVE: un
 * proveedor DISABLED no debe poder seleccionarse para nuevas búsquedas.
 */
public interface ProviderService {

    List<ProviderResponse> listActive();
}
