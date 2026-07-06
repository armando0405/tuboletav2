import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/utils/services/authServices'
import type { User } from '@/types/services/User'

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const isCheckingAuth = ref<boolean>(false)

    const isAuthenticated = computed<boolean>((): boolean => user.value !== null)
    const isAdmin = computed<boolean>((): boolean => user.value?.role === 'ADMIN')

    function setUser(userData: User): void {
        user.value = userData
    }

    function clearUser(): void {
        user.value = null
    }

    async function login(email: string, password: string): Promise<User> {
        const { data } = await authService.postLogin({ email, password })
        if (!data.object) throw new Error('El login no devolvió un usuario')
        setUser(data.object)
        return data.object
    }

    async function logout(): Promise<void> {
        try {
            await authService.postLogout()
        } finally {
            clearUser()
        }
    }

    async function getUserLogged(): Promise<boolean> {
        isCheckingAuth.value = true
        try {
            const { data } = await authService.getMe()
            user.value = data.object ?? null
        } catch {
            user.value = null
        } finally {
            isCheckingAuth.value = false
        }
        return isAuthenticated.value
    }

    return {
        user,
        isAuthenticated,
        isAdmin,
        isCheckingAuth,
        setUser,
        clearUser,
        login,
        logout,
        getUserLogged,
    }
})
