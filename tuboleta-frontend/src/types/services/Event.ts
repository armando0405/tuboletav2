// Refleja EventResponse (SearchController#events, REQ-DET-001/003). Fechas
// Instant/LocalDate del backend llegan como string ISO serializadas por Jackson.
export type EventStatus = 'ACTIVE' | 'REMOVED'

export type Event = {
    id: number
    providerName: string
    externalId: string
    title: string
    venue: string | null
    eventDateRaw: string | null
    eventDate: string | null
    status: EventStatus
    missCount: number
    firstSeenAt: string
    lastSeenAt: string
    // Nº de cambios registrados (event_changes): si > 0, el evento tiene
    // historial y se muestra el botón para verlo (REQ-DET-002).
    changesCount: number
}
