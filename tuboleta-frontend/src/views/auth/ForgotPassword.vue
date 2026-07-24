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
                            <h6 class="text-h6 font-weight-medium text-center mt-3 mb-1">
                                Recuperar contraseña
                            </h6>

                            <template v-if="!sent">
                                <p class="text-body-2 text-medium-emphasis text-center mb-6">
                                    Escribe tu correo y te enviaremos un enlace para restablecerla.
                                </p>
                                <form @submit.prevent="submit">
                                    <v-text-field
                                        label="Correo electrónico"
                                        type="email"
                                        prepend-inner-icon="mdi-account"
                                        v-model="email"
                                        required
                                        :disabled="submitting"
                                    />
                                    <v-btn
                                        color="primary"
                                        size="large"
                                        block
                                        variant="flat"
                                        type="submit"
                                        class="mt-2"
                                        :loading="submitting"
                                    >
                                        Enviar enlace
                                    </v-btn>
                                </form>
                            </template>

                            <div
                                v-else
                                class="text-center py-4"
                            >
                                <v-icon
                                    icon="mdi-email-check-outline"
                                    color="success"
                                    size="48"
                                    class="mb-3"
                                />
                                <p class="text-body-1 mb-1">Revisa tu correo</p>
                                <p class="text-body-2 text-medium-emphasis mb-0">
                                    Si <strong>{{ email }}</strong> está registrado, te enviamos un
                                    enlace para restablecer tu contraseña (vence en 1 hora).
                                </p>
                            </div>

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
import { ref } from 'vue'
import { usePasswordRecovery } from '@/composables/account/usePasswordRecovery'

const { submitting, forgotPassword } = usePasswordRecovery()
const email = ref<string>('')
const sent = ref<boolean>(false)

const submit = async () => {
    if (!email.value) return
    const ok = await forgotPassword(email.value)
    if (ok) sent.value = true
}
</script>
