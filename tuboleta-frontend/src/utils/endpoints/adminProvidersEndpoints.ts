// AdminProviderController: /api/admin/providers/** (solo ADMIN, REQ-FUE-001/002)
const CONTROLLER_ADMIN_PROVIDERS = '/admin/providers'

const GET_ADMIN_PROVIDERS = CONTROLLER_ADMIN_PROVIDERS
const POST_DISABLE_PROVIDER = (id: number): string => `${CONTROLLER_ADMIN_PROVIDERS}/${id}/disable`
const POST_ENABLE_PROVIDER = (id: number): string => `${CONTROLLER_ADMIN_PROVIDERS}/${id}/enable`

export { GET_ADMIN_PROVIDERS, POST_DISABLE_PROVIDER, POST_ENABLE_PROVIDER }
