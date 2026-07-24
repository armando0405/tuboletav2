// Enums de proveedor compartidos entre SearchProviderInfo y ProviderAdminResponse.

export type ProviderType = 'SCRAPER' | 'API'

export type ProviderStatus = 'ACTIVE' | 'DISABLED'

// Refleja ProviderResponse (ProviderController, REQ-FUE-001): catálogo
// público de proveedores ACTIVE, para el multi-select al crear una búsqueda.
// Sin status/statusReason — eso es exclusivo de ProviderAdmin (solo ADMIN).
export type Provider = {
    id: number
    name: string
    type: ProviderType
}
