// Refleja EventChangeResponse: una entrada del historial de cambios de un
// evento (REQ-DET-002). fieldLabel viene en español listo para mostrar.
export type EventChange = {
    id: number
    fieldName: string
    fieldLabel: string
    oldValue: string | null
    newValue: string | null
    detectedAt: string
}
