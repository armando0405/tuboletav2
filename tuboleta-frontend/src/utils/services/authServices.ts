import api from '@/plugins/axios'
import { AUTH } from '@/utils/endpoints'
import type { ObjectResponse } from '@/types/services/Responses'
import type { User } from '@/types/services/User'
import type { LoginRequest, RegisterRequest } from '@/types/services/Auth'
import type { AxiosResponse } from 'axios'

const postRegister = (payload: RegisterRequest): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.post<ObjectResponse<User>>(AUTH.POST_REGISTER, payload)

const postLogin = (payload: LoginRequest): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.post<ObjectResponse<User>>(AUTH.POST_LOGIN, payload)

const postLogout = (): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(AUTH.POST_LOGOUT)

const getMe = (): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.get<ObjectResponse<User>>(AUTH.GET_ME)

export const authService = {
    postRegister,
    postLogin,
    postLogout,
    getMe,
}
