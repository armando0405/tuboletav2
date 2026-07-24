// Payloads de AuthController (LoginRequest/RegisterRequest). La respuesta en
// ambos casos es UserResponse -> tipo `User` (ver ./User.ts).

export type LoginRequest = {
    email: string
    password: string
}

export type RegisterRequest = {
    email: string
    name: string
    password: string
}

export type ChangePasswordRequest = {
    currentPassword: string
    newPassword: string
}

export type UpdateProfileRequest = {
    name: string
    email: string
}

export type ForgotPasswordRequest = {
    email: string
}

export type ResetPasswordRequest = {
    token: string
    newPassword: string
}
