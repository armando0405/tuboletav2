<template>
    <!-- ---------------------------------------------- -->
    <!-- notifications DD -->
    <!-- ---------------------------------------------- -->
    <v-menu :close-on-content-click="false">
        <template #activator="{ props }">
            <v-btn
                class="profileBtn custom-hover-primary"
                variant="text"
                v-bind="props"
                icon
            >
                <v-avatar
                    color="primary"
                    size="35"
                >
                    <span class="text-white text-caption font-weight-bold">{{ initial }}</span>
                </v-avatar>
            </v-btn>
        </template>
        <v-sheet
            rounded="md"
            width="200"
            elevation="10"
            class="mt-2"
        >
            <div class="pt-4 pb-4 px-5 text-center">
                <v-btn
                    color="primary"
                    variant="outlined"
                    block
                    @click="logout"
                >
                    Cerrar Sesión
                </v-btn>
            </div>
        </v-sheet>
    </v-menu>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth.store'
import { router } from '@/router'
import { securityServices } from '@/utils/services/securityServices'
import { computed } from 'vue'

const { postLogout } = securityServices
const authStore = useAuthStore()

const initial = computed<string>(() => {
    const name = authStore?.user?.name ?? ''

    return name
        .split(' ')
        .filter(Boolean)
        .map((n: string) => n[0])
        .slice(0, 2)
        .join('')
        .toUpperCase()
})

const logout = async (): Promise<void> => {
    try {
        await postLogout()
        authStore.clearUser()
        router.push({ name: 'login' })
    } catch (error) {
        console.error(error)
    }
}
</script>
