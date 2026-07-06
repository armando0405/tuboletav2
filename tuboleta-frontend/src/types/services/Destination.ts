// Refleja DestinationResponse/DestinationRequest (DestinationController).
// Único canal soportado hoy es EMAIL (REQ-NOT-001); sin delete físico, solo
// activar/desactivar.

export type Destination = {
    id: number
    channelName: string
    destination: string
    isActive: boolean
}

export type DestinationRequest = {
    destination: string
}
