package com.tuboleta.backend.domain.enums;

/**
 * Estrategia de extracción del proveedor: SCRAPER (se scrapea HTML)
 * o API (se consume una API oficial).
 */
public enum ProviderType {
    SCRAPER,
    API
}
