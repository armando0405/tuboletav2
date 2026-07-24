// AuthController: /api/auth/** (el baseURL '/api' se fija en plugins/axios.ts).
const BASE_API = '/auth'

const POST_REGISTER = `${BASE_API}/register`
const POST_LOGIN = `${BASE_API}/login`
const POST_LOGOUT = `${BASE_API}/logout`
const GET_ME = `${BASE_API}/me`
const PATCH_PASSWORD = `${BASE_API}/password`
const PATCH_PROFILE = `${BASE_API}/profile`
const POST_FORGOT_PASSWORD = `${BASE_API}/password/forgot`
const POST_RESET_PASSWORD = `${BASE_API}/password/reset`

export {
    POST_REGISTER,
    POST_LOGIN,
    POST_LOGOUT,
    GET_ME,
    PATCH_PASSWORD,
    PATCH_PROFILE,
    POST_FORGOT_PASSWORD,
    POST_RESET_PASSWORD,
}
