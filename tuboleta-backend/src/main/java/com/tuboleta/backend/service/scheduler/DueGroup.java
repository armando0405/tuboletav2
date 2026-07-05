package com.tuboleta.backend.service.scheduler;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.SearchProvider;
import java.util.List;

/**
 * Un grupo de trabajo vencido para el scheduler (REQ-DET-005): todos los
 * pares búsqueda↔proveedor que comparten el mismo {@code (provider, term_normalized)}
 * se resuelven con UNA sola extracción, cuyo resultado se aplica a cada par
 * (el diff sí es individual, cada par tiene su propio histórico en {@code events}).
 *
 * @param provider        proveedor común a todos los pares del grupo
 * @param termNormalized  término normalizado común a todos los pares del grupo
 * @param pairs           pares búsqueda↔proveedor vencidos que comparten provider+término
 */
public record DueGroup(Provider provider, String termNormalized, List<SearchProvider> pairs) {
}
