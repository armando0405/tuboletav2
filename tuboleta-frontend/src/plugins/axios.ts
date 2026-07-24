import { router } from '@/router'
import { useAuthStore } from '@/stores/auth.store'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import { useNotifyStore } from '@/stores/notify.store'
import axios from 'axios'
import type { AxiosResponse, AxiosError, AxiosInstance } from 'axios'
// Base de la API: mismo origen que el SPA (el backend sirve el front y expone
// /api/**). Se fija aquí, en la capa de servicios, en vez de depender de una
// variable de entorno de build. En dev, vite.config proxya /api -> :8088.
const API_BASE_URL = '/api'
const API_TIMEOUT_MS = 100_000

const instance: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    timeout: API_TIMEOUT_MS,
    withCredentials: true,
})

let isRedirectingToLogin: boolean = false

instance.interceptors.response.use(
    (response: AxiosResponse<ObjectListResponse | ObjectResponse>) => {
        if (response.data?.code === -1) {
            const msg: string = response.data.msg || 'Error de proceso interno (-1)'
            useNotifyStore().notify(msg, 'error')
            return Promise.reject(new Error(msg))
        }
        return response
    },
    (error: AxiosError<ObjectListResponse | ObjectResponse>) => {
        const msg: string = error?.response?.data?.msg || 'Error en la petición'
        if (error?.response?.status === 401) {
            const authStore = useAuthStore()
            authStore.clearUser()
            useNotifyStore().notify(msg, 'error')
            if (!isRedirectingToLogin && !authStore.isCheckingAuth) {
                isRedirectingToLogin = true
                router.replace({ name: 'login' }).finally(() => {
                    isRedirectingToLogin = false
                })
            }

            return Promise.reject(error)
        }

        useNotifyStore().notify(msg, 'error')

        return Promise.reject(error)
    },
)

export default instance
