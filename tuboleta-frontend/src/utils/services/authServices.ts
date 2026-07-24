import api from '@/plugins/axios'
import { AUTH } from '@/utils/endpoints'
import type { ObjectResponse } from '@/types/services/Responses'
import type { User } from '@/types/services/User'
import type {
    LoginRequest,
    RegisterRequest,
    ChangePasswordRequest,
    UpdateProfileRequest,
    ForgotPasswordRequest,
    ResetPasswordRequest,
} from '@/types/services/Auth'
import type { AxiosResponse } from 'axios'

const postRegister = (payload: RegisterRequest): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.post<ObjectResponse<User>>(AUTH.POST_REGISTER, payload)

const postLogin = (payload: LoginRequest): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.post<ObjectResponse<User>>(AUTH.POST_LOGIN, payload)

const postLogout = (): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(AUTH.POST_LOGOUT)

const getMe = (): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.get<ObjectResponse<User>>(AUTH.GET_ME)

const patchPassword = (
    payload: ChangePasswordRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.patch<ObjectResponse<void>>(AUTH.PATCH_PASSWORD, payload)

const patchProfile = (
    payload: UpdateProfileRequest,
): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.patch<ObjectResponse<User>>(AUTH.PATCH_PROFILE, payload)

const postForgotPassword = (
    payload: ForgotPasswordRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(AUTH.POST_FORGOT_PASSWORD, payload)

const postResetPassword = (
    payload: ResetPasswordRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(AUTH.POST_RESET_PASSWORD, payload)

export const authService = {
    postRegister,
    postLogin,
    postLogout,
    getMe,
    patchPassword,
    patchProfile,
    postForgotPassword,
    postResetPassword,
}
