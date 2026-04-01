<template>
    <div class="authentication">
        <v-container
            fluid
            class="pa-3"
        >
            <v-row class="h-100vh d-flex justify-center align-center">
                <form @submit.prevent="login">
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
                                <h6
                                    class="text-h6 text-muted font-weight-medium d-flex justify-center align-center mt-3"
                                >
                                    TASRET
                                </h6>

                                <v-row class="d-flex mb-8">
                                    <v-col cols="12">
                                        <v-text-field
                                            label="Usuario"
                                            prepend-inner-icon="mdi-account"
                                            v-model="form.username"
                                            required
                                            :disabled="loading"
                                        />
                                    </v-col>

                                    <v-col cols="12">
                                        <v-text-field
                                            label="Contraseña"
                                            :append-inner-icon="visible ? 'mdi-eye-off' : 'mdi-eye'"
                                            v-model="form.password"
                                            prepend-inner-icon="mdi-lock"
                                            required
                                            :type="visible ? 'text' : 'password'"
                                            @click:append-inner="visible = !visible"
                                            :disabled="loading"
                                        />
                                    </v-col>

                                    <v-col
                                        cols="12"
                                        class="pt-0"
                                    >
                                        <v-btn
                                            color="primary"
                                            size="large"
                                            block
                                            flat
                                            type="submit"
                                            :disabled="loading"
                                        >
                                            Iniciar sesión
                                        </v-btn>
                                    </v-col>
                                </v-row>
                            </v-card-item>
                            <v-progress-linear
                                v-if="loading"
                                indeterminate
                                color="primary"
                            />
                        </v-card>
                    </v-col>
                </form>
            </v-row>
        </v-container>
    </div>
</template>

<script setup lang="ts">
import Logo from '@/layouts/full/logo/Logo.vue'

import { router } from '@/router'
import type { LoginDTO } from '@/types'
import { securityServices } from '@/utils/services/securityServices'
import { ref } from 'vue'

const { postLogin } = securityServices

const visible = ref<boolean>(false)
const loading = ref<boolean>(false)
const form = ref<LoginDTO>({
    username: '',
    password: '',
})

const login = async () => {
    try {
        loading.value = true
        await postLogin(form.value)
        router.push({ name: 'home' })
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}
</script>
