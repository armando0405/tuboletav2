export type UserRole = 'ADMIN' | 'USER'

// Refleja UserResponse (backend): register/login/me. Nunca trae password.
export type User = {
    id: number
    email: string
    name: string
    role: UserRole
}
