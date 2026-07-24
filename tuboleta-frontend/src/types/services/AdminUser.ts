import type { UserRole } from './User'

export type UserStatus = 'ACTIVE' | 'INACTIVE'

// Refleja AdminUserResponse (AdminUserController): fila del panel admin.
export type AdminUser = {
    id: number
    email: string
    name: string
    role: UserRole
    status: UserStatus
    createdAt: string
}
