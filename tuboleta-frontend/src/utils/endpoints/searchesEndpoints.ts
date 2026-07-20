// SearchController: /api/searches/**
const CONTROLLER_SEARCHES = '/searches'

const POST_SEARCH = CONTROLLER_SEARCHES
const GET_SEARCHES = CONTROLLER_SEARCHES
const GET_SEARCH_EVENTS = (id: number): string => `${CONTROLLER_SEARCHES}/${id}/events`
const GET_SEARCH_EVENT_CHANGES = (id: number, eventId: number): string =>
    `${CONTROLLER_SEARCHES}/${id}/events/${eventId}/changes`
const PATCH_SEARCH = (id: number): string => `${CONTROLLER_SEARCHES}/${id}`
const PATCH_TOGGLE_SEARCH_PROVIDER = (id: number, providerId: number): string =>
    `${CONTROLLER_SEARCHES}/${id}/providers/${providerId}/toggle`
// Pausa/reanuda la búsqueda COMPLETA (todos sus proveedores a la vez).
const PATCH_TOGGLE_SEARCH_STATUS = (id: number): string => `${CONTROLLER_SEARCHES}/${id}/toggle`
// Dispara la corrida de monitoreo AHORA, sin esperar el horario.
const POST_RUN_SEARCH_NOW = (id: number): string => `${CONTROLLER_SEARCHES}/${id}/run-now`
const DELETE_SEARCH = (id: number): string => `${CONTROLLER_SEARCHES}/${id}`

export {
    POST_SEARCH,
    GET_SEARCHES,
    GET_SEARCH_EVENTS,
    GET_SEARCH_EVENT_CHANGES,
    PATCH_SEARCH,
    PATCH_TOGGLE_SEARCH_PROVIDER,
    PATCH_TOGGLE_SEARCH_STATUS,
    POST_RUN_SEARCH_NOW,
    DELETE_SEARCH,
}
