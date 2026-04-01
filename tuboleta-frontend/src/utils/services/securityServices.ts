import api from '@/plugins/axios'
import { SECURITY } from '@/utils/endpoints'
import type { ObjectResponse, LoginDTO, User } from '@/types'
import type { AxiosResponse } from 'axios'

const postLogin = (payload: LoginDTO): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.post<ObjectResponse<User>>(SECURITY.POST_LOGIN, payload)

const postLogout = (): Promise<AxiosResponse<ObjectResponse>> =>
    api.post<ObjectResponse>(SECURITY.POST_LOGOUT)

const getInfoByUserLogged = (): Promise<AxiosResponse<ObjectResponse<User>>> =>
    api.get<ObjectResponse<User>>(SECURITY.GET_INFO_BY_USER_LOGGED)

export const securityServices = {
    postLogin,
    postLogout,
    getInfoByUserLogged,
}
