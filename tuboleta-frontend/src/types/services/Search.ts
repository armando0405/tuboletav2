import type { ProviderStatus } from './Provider'

export type SearchStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED'

// Refleja SearchProviderInfo: par búsqueda<->proveedor. El estado EFECTIVO
// (pausado por el usuario vs. fuente deshabilitada por ADMIN) lo arma el
// frontend combinando pairActive + providerStatus (REQ-FE-002) — el backend
// solo entrega los datos crudos.
export type SearchProviderInfo = {
    providerId: number
    providerName: string
    pairActive: boolean
    providerStatus: ProviderStatus
    // Motivo (posiblemente null) por el que el ADMIN deshabilitó la fuente;
    // solo tiene sentido cuando providerStatus === 'DISABLED' (REQ-FUE-002).
    providerStatusReason: string | null
    lastRunAt: string | null
}

// Refleja SearchResponse. destinationIds son los destinos ACTIVOS asociados
// hoy (para precargar el multi-select al editar) y eventsCount es el total
// de eventos de todos los pares de la búsqueda (contador de la card).
export type Search = {
    id: number
    term: string
    // Cada cuántos MINUTOS se monitorea (elegido del catálogo de frecuencias).
    checkFrequencyMinutes: number
    status: SearchStatus
    providers: SearchProviderInfo[]
    destinationIds: number[]
    eventsCount: number
}

// Refleja SearchCreateRequest: al menos un providerId es obligatorio,
// destinationIds no. La frecuencia va en minutos (valor del catálogo activo).
export type SearchCreateRequest = {
    term: string
    checkFrequencyMinutes: number
    providerIds: number[]
    destinationIds?: number[]
}

// Refleja SearchUpdateRequest: ambos campos opcionales, null/undefined
// significa "no tocar este campo".
export type SearchUpdateRequest = {
    checkFrequencyMinutes?: number | null
    destinationIds?: number[] | null
}
