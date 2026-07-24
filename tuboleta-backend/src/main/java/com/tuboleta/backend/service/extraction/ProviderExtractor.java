package com.tuboleta.backend.service.extraction;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.utils.exception.ScrapingException;

/**
 * Contrato de extracción por proveedor (REQ-FUE-001).
 *
 * <p>Una implementación NO filtra por término de búsqueda ni toca la base
 * de datos: extrae todo lo que el proveedor devuelve para la URL armada con
 * {@code normalizedTerm}. El filtrado (REQ-BUS-003) es responsabilidad de
 * la capa de detección de cambios.</p>
 */
public interface ProviderExtractor {

    /**
     * Indica si esta implementación sabe extraer eventos del proveedor dado.
     *
     * @param provider proveedor a evaluar
     * @return {@code true} si esta implementación aplica a {@code provider}
     */
    boolean supports(Provider provider);

    /**
     * Extrae los eventos publicados por el proveedor para el término dado.
     *
     * @param provider       proveedor del cual extraer
     * @param normalizedTerm término ya normalizado, usado SOLO para armar la
     *                       URL de búsqueda (reemplaza {@code {term}} en
     *                       {@code search_url}, URL-encoded) — no se usa para
     *                       filtrar resultados
     * @return el resultado de la extracción, incluyendo ítems fallidos
     * @throws ScrapingException si la extracción falla a nivel de página completa
     *                           (p.ej. no se pudo conectar o descargar el HTML)
     */
    ExtractionResult extract(Provider provider, String normalizedTerm) throws ScrapingException;
}
