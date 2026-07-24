import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/utils/services/authServices'
import { useSearches } from '@/composables/searches/useSearches'
import { useSearchEvents } from '@/composables/searches/useSearchEvents'
import { useNotifications } from '@/composables/notifications/useNotifications'
import { useDestinations } from '@/composables/destinations/useDestinations'
import { useProvidersAdmin } from '@/composables/admin/useProvidersAdmin'
import { useFrequenciesAdmin } from '@/composables/admin/useFrequenciesAdmin'
import { useAdminUsers } from '@/composables/admin/useAdminUsers'
import type { User } from '@/types/services/User'

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const isCheckingAuth = ref<boolean>(false)

    const isAuthenticated = computed<boolean>((): boolean => user.value !== null)
    const isAdmin = computed<boolean>((): boolean => user.value?.role === 'ADMIN')

    function setUser(userData: User): void {
        user.value = userData
    }

    // Se llama tanto en logout explícito como cuando el interceptor de axios
    // detecta un 401 (sesión expirada) — en ambos casos hay que limpiar
    // también el estado module-level de los composables de dominio, o el
    // siguiente usuario que inicie sesión en la misma pestaña vería datos
    // (búsquedas, notificaciones, destinos, fuentes) de la sesión anterior.
    function clearUser(): void {
        user.value = null
        useSearches().resetAll()
        useSearchEvents().resetAll()
        useNotifications().resetAll()
        useDestinations().resetAll()
        useProvidersAdmin().resetAll()
        useFrequenciesAdmin().resetAll()
        useAdminUsers().resetAll()
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
