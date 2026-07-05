package com.tuboleta.backend.domain.enums;

/**
 * Estado de la búsqueda. DELETED es siempre eliminación lógica
 * (nunca DELETE físico); INACTIVE hace que el scheduler la salte.
 */
public enum SearchStatus {
    ACTIVE,
    INACTIVE,
    DELETED
}
