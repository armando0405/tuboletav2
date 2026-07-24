// AdminUserController: /api/admin/users/** (solo ADMIN).
const BASE_API = '/admin/users'

const GET_USERS = BASE_API
const PATCH_USER_STATUS = (id: number): string => `${BASE_API}/${id}/status`
const PATCH_USER_ROLE = (id: number): string => `${BASE_API}/${id}/role`

export { GET_USERS, PATCH_USER_STATUS, PATCH_USER_ROLE }
