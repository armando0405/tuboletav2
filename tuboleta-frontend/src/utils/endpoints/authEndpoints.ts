// AuthController: /api/auth/** (VITE_API_URL ya incluye /api).
const BASE_API = '/auth'

const POST_REGISTER = `${BASE_API}/register`
const POST_LOGIN = `${BASE_API}/login`
const POST_LOGOUT = `${BASE_API}/logout`
const GET_ME = `${BASE_API}/me`

export { POST_REGISTER, POST_LOGIN, POST_LOGOUT, GET_ME }
