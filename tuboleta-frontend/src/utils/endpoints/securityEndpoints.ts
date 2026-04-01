const BASE_API = '/security'

const CONTROLLER_AUTH = `${BASE_API}/auth`
const CONTROLLER_USER = `${BASE_API}/user`

const POST_LOGIN = `${CONTROLLER_AUTH}/login`
const POST_LOGOUT = `${CONTROLLER_AUTH}/logout`
const GET_INFO_BY_USER_LOGGED = `${CONTROLLER_USER}/get-info-by-user-logged`

export { POST_LOGIN, POST_LOGOUT, GET_INFO_BY_USER_LOGGED }
