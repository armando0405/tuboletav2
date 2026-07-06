import type { ProviderStatus } from './Provider'

export type SearchStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED'

// Frecuencias cerradas admitidas por el backend (SearchCreateRequest/SearchUpdateRequest).
export type CheckFrequencyHours = 6 | 12 | 24 | 48

// Refleja SearchProviderInfo: par búsqueda<->proveedor. El estado EFECTIVO
// (pausado por el usuario vs. fuente deshabilitada por ADMIN) lo arma el
// frontend combinando pairActive + providerStatus (REQ-FE-002) — el backend
// solo entrega los datos crudos.
export type SearchProviderInfo = {
    providerId: number
    providerName: string
    pairActive: boolean
    providerStatus: ProviderStatus
    lastRunAt: string | null
}

// Refleja SearchResponse.
export type Search = {
    id: number
    term: string
    checkFrequencyHours: number
    status: SearchStatus
    providers: SearchProviderInfo[]
}

// Refleja SearchCreateRequest: al menos un providerId es obligatorio,
// destinationIds no.
export type SearchCreateRequest = {
    term: string
    checkFrequencyHours: CheckFrequencyHours
    providerIds: number[]
    destinationIds?: number[]
}

// Refleja SearchUpdateRequest: ambos campos opcionales, null/undefined
// significa "no tocar este campo".
export type SearchUpdateRequest = {
    checkFrequencyHours?: CheckFrequencyHours | null
    destinationIds?: number[] | null
}
