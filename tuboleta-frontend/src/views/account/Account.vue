<template>
    <div>
        <h1 class="text-h5 font-weight-bold mb-1">Mi cuenta</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">
            Edita tu perfil y cambia tu contraseña.
        </p>

        <v-row>
            <!-- Perfil -->
            <v-col
                cols="12"
                md="6"
            >
                <v-card
                    rounded="lg"
                    variant="flat"
                    border
                >
                    <v-card-title class="text-subtitle-1 font-weight-bold pt-5 px-6">
                        <v-icon
                            icon="mdi-account-outline"
                            class="me-2"
                        />
                        Perfil
                    </v-card-title>
                    <v-card-text class="px-6">
                        <form @submit.prevent="saveProfile">
                            <v-text-field
                                label="Nombre"
                                v-model="profile.name"
                                prepend-inner-icon="mdi-account"
                                :disabled="submitting"
                            />
                            <v-text-field
                                label="Correo electrónico"
                                type="email"
                                v-model="profile.email"
                                prepend-inner-icon="mdi-email"
                                :disabled="submitting"
                                class="mt-2"
                            />
                            <v-btn
                                color="primary"
                                variant="flat"
                                type="submit"
                                class="mt-2"
                                :loading="submitting"
                                :disabled="!profile.name || !profile.email"
                            >
                                Guardar perfil
                            </v-btn>
                        </form>
                    </v-card-text>
                </v-card>
            </v-col>

            <!-- Cambiar contraseña -->
            <v-col
                cols="12"
                md="6"
            >
                <v-card
                    rounded="lg"
                    variant="flat"
                    border
                >
                    <v-card-title class="text-subtitle-1 font-weight-bold pt-5 px-6">
                        <v-icon
                            icon="mdi-lock-outline"
                            class="me-2"
                        />
                        Cambiar contraseña
                    </v-card-title>
                    <v-card-text class="px-6">
                        <form @submit.prevent="savePassword">
                            <v-text-field
                                label="Contraseña actual"
                                type="password"
                                v-model="pwd.current"
                                prepend-inner-icon="mdi-lock"
                                :disabled="submitting"
                            />
                            <v-text-field
                                label="Nueva contraseña"
                                type="password"
                                v-model="pwd.next"
                                prepend-inner-icon="mdi-lock-plus"
                                hint="Mínimo 8 caracteres"
                                persistent-hint
                                :disabled="submitting"
                                class="mt-2"
                            />
                            <v-text-field
                                label="Confirmar nueva contraseña"
                                type="password"
                                v-model="pwd.confirm"
                                prepend-inner-icon="mdi-lock-check"
                                :error-messages="pwdMismatch ? ['Las contraseñas no coinciden'] : []"
                                :disabled="submitting"
                                class="mt-3"
                            />
                            <v-btn
                                color="primary"
                                variant="flat"
                                type="submit"
                                class="mt-2"
                                :loading="submitting"
                                :disabled="!canChangePwd"
                            >
                                Actualizar contraseña
                            </v-btn>
                        </form>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </div>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { useAccount } from '@/composables/account/useAccount'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
const { submitting, changePassword, updateProfile } = useAccount()

const profile = reactive({
    name: authStore.user?.name ?? '',
    email: authStore.user?.email ?? '',
})

const pwd = reactive({ current: '', next: '', confirm: '' })

const pwdMismatch = computed<boolean>(
    () => pwd.confirm.length > 0 && pwd.next !== pwd.confirm,
)
const canChangePwd = computed<boolean>(
    () => pwd.current.length > 0 && pwd.next.length >= 8 && pwd.next === pwd.confirm,
)

const saveProfile = async () => {
    if (!profile.name || !profile.email) return
    await updateProfile({ name: profile.name, email: profile.email })
}

const savePassword = async () => {
    if (!canChangePwd.value) return
    const ok = await changePassword({ currentPassword: pwd.current, newPassword: pwd.next })
    if (ok) {
        pwd.current = ''
        pwd.next = ''
        pwd.confirm = ''
    }
}
</script>
