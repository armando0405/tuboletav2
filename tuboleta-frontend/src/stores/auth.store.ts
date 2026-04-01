import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { securityServices } from '@/utils/services/securityServices'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const isCheckingAuth = ref<boolean>(false)

    const isAuthenticated = computed<boolean>((): boolean => user.value !== null)

    function setUser(userData: User): void {
        user.value = userData
    }

    function clearUser(): void {
        user.value = null
    }

    async function getUserLogged(): Promise<boolean> {
        isCheckingAuth.value = true
        try {
            const { data } = await securityServices.getInfoByUserLogged()
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
        isCheckingAuth,
        setUser,
        clearUser,
        getUserLogged,
    }
})
