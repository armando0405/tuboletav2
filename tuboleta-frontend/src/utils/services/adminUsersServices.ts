import api from '@/plugins/axios'
import { ADMIN_USERS } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type { AdminUser } from '@/types/services/AdminUser'
import type { UserRole } from '@/types/services/User'
import type { AxiosResponse } from 'axios'

const getUsers = (): Promise<AxiosResponse<ObjectListResponse<AdminUser>>> =>
    api.get<ObjectListResponse<AdminUser>>(ADMIN_USERS.GET_USERS)

const patchUserStatus = (
    id: number,
    active: boolean,
): Promise<AxiosResponse<ObjectResponse<AdminUser>>> =>
    api.patch<ObjectResponse<AdminUser>>(ADMIN_USERS.PATCH_USER_STATUS(id), { active })

const patchUserRole = (
    id: number,
    role: UserRole,
): Promise<AxiosResponse<ObjectResponse<AdminUser>>> =>
    api.patch<ObjectResponse<AdminUser>>(ADMIN_USERS.PATCH_USER_ROLE(id), { role })

export const adminUsersService = {
    getUsers,
    patchUserStatus,
    patchUserRole,
}
