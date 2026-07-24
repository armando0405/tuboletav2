// FrequencyController: /api/frequencies (usuario, activas)
const GET_FREQUENCIES = '/frequencies'

// AdminFrequencyController: /api/admin/frequencies (CRUD, solo ADMIN)
const CONTROLLER_ADMIN_FREQUENCIES = '/admin/frequencies'
const GET_ADMIN_FREQUENCIES = CONTROLLER_ADMIN_FREQUENCIES
const POST_ADMIN_FREQUENCY = CONTROLLER_ADMIN_FREQUENCIES
const PUT_ADMIN_FREQUENCY = (id: number): string => `${CONTROLLER_ADMIN_FREQUENCIES}/${id}`
const DELETE_ADMIN_FREQUENCY = (id: number): string => `${CONTROLLER_ADMIN_FREQUENCIES}/${id}`

export {
    GET_FREQUENCIES,
    GET_ADMIN_FREQUENCIES,
    POST_ADMIN_FREQUENCY,
    PUT_ADMIN_FREQUENCY,
    DELETE_ADMIN_FREQUENCY,
}
