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
