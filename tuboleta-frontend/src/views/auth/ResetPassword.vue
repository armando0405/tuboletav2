<template>
    <div class="authentication">
        <v-container
            fluid
            class="pa-3"
        >
            <v-row class="h-100vh d-flex justify-center align-center">
                <v-col
                    cols="12"
                    lg="4"
                    xl="3"
                    class="d-flex align-center"
                >
                    <v-card
                        rounded="md"
                        elevation="10"
                        class="px-sm-1 px-0 withbg mx-auto"
                        max-width="500"
                    >
                        <v-card-item class="pa-sm-8">
                            <div class="d-flex justify-center py-4">
                                <Logo />
                            </div>
                            <h6 class="text-h6 font-weight-medium text-center mt-3 mb-6">
                                Nueva contraseña
                            </h6>

                            <v-alert
                                v-if="!token"
                                type="error"
                                variant="tonal"
                                class="mb-4"
                            >
                                El enlace no es válido. Solicita uno nuevo desde
                                "Recuperar contraseña".
                            </v-alert>

                            <form
                                v-else
                                @submit.prevent="submit"
                            >
                                <v-text-field
                                    label="Nueva contraseña"
                                    :type="visible ? 'text' : 'password'"
                                    prepend-inner-icon="mdi-lock"
                                    :append-inner-icon="visible ? 'mdi-eye-off' : 'mdi-eye'"
                                    @click:append-inner="visible = !visible"
                                    v-model="password"
                                    :disabled="submitting"
                                    hint="Mínimo 8 caracteres"
                                    persistent-hint
                                />
                                <v-text-field
                                    label="Confirmar contraseña"
                                    :type="visible ? 'text' : 'password'"
                                    prepend-inner-icon="mdi-lock-check"
                                    v-model="confirm"
                                    :disabled="submitting"
                                    :error-messages="mismatch ? ['Las contraseñas no coinciden'] : []"
                                    class="mt-3"
                                />
                                <v-btn
                                    color="primary"
                                    size="large"
                                    block
                                    variant="flat"
                                    type="submit"
                                    class="mt-2"
                                    :loading="submitting"
                                    :disabled="!canSubmit"
                                >
                                    Restablecer contraseña
                                </v-btn>
                            </form>

                            <div class="text-center mt-6">
                                <router-link
                                    :to="{ name: 'login' }"
                                    class="text-primary text-decoration-none text-body-2"
                                >
                                    ← Volver a iniciar sesión
                                </router-link>
                            </div>
                        </v-card-item>
                    </v-card>
                </v-col>
            </v-row>
        </v-container>
    </div>
</template>

<script setup lang="ts">
import Logo from '@/layouts/full/logo/Logo.vue'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { router } from '@/router'
import { usePasswordRecovery } from '@/composables/account/usePasswordRecovery'

const route = useRoute()
const { submitting, resetPassword } = usePasswordRecovery()

const token = computed<string>(() => (route.query.token as string) || '')
const password = ref<string>('')
const confirm = ref<string>('')
const visible = ref<boolean>(false)

const mismatch = computed<boolean>(() => confirm.value.length > 0 && password.value !== confirm.value)
const canSubmit = computed<boolean>(
    () => password.value.length >= 8 && password.value === confirm.value,
)

const submit = async () => {
    if (!token.value || !canSubmit.value) return
    const ok = await resetPassword(token.value, password.value)
    if (ok) router.push({ name: 'login' })
}
</script>
