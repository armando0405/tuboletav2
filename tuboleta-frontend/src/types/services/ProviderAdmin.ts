import type { ProviderStatus, ProviderType } from './Provider'

// Refleja ProviderAdminResponse (AdminProviderController, REQ-FUE-001/002),
// solo ADMIN.
export type ProviderAdmin = {
    id: number
    name: string
    providerType: ProviderType
    baseUrl: string
    searchUrl: string | null
    config: string | null
    status: ProviderStatus
    statusReason: string | null
    statusChangedAt: string | null
}

// Refleja ProviderDisableRequest.
export type ProviderDisableRequest = {
    reason: string
}

// Refleja ProviderCreateRequest / ProviderUpdateRequest (alta/edición).
export type ProviderSaveRequest = {
    name: string
    providerType: ProviderType
    baseUrl: string
    searchUrl: string
    config: string | null
}
