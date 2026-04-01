import { router } from '@/router'
import { useAuthStore } from '@/stores/auth.store'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import { useNotifyStore } from '@/stores/notify.store'
import axios from 'axios'
import type { AxiosResponse, AxiosError, AxiosInstance } from 'axios'
const instance: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_URL as string,
    timeout: import.meta.env.VITE_API_TIMEOUT as number,
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
