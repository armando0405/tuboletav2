<template>
    <div class="d-flex flex-wrap align-center justify-space-between ga-4 mb-6">
        <div>
            <h1 class="text-h4 font-weight-bold mb-1">Destinos de notificación</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
                Correos donde recibir alertas. Se eligen al crear o editar una búsqueda.
            </p>
        </div>

        <v-btn
            color="primary"
            prepend-icon="mdi-plus"
            @click="openCreateDialog"
        >
            Agregar destino
        </v-btn>
    </div>

    <v-dialog
        v-model="showFormDialog"
        max-width="480"
        @update:model-value="(val) => !val && closeFormDialog()"
    >
        <v-card class="dialog-surface">
            <v-card-title class="d-flex align-center ga-2 pt-5 px-6">
                <v-icon
                    icon="mdi-email-plus-outline"
                    color="primary"
                />
                <span class="text-h6 font-weight-bold">Nuevo destino</span>
            </v-card-title>

            <v-form
                ref="formRef"
                v-model="formValid"
                @submit.prevent="submit"
            >
                <v-card-text class="px-6">
                    <v-alert
                        v-if="formError"
                        type="error"
                        variant="tonal"
                        density="compact"
                        class="mb-4"
                    >
                        {{ formError }}
                    </v-alert>

                    <v-text-field
                        v-model="email"
                        label="Correo electrónico"
                        placeholder="nombre@correo.com"
                        type="email"
                        autocomplete="email"
                        :rules="[rules.required, rules.email]"
                        autofocus
                        hint="Este correo estará disponible al crear o editar tus búsquedas."
                        persistent-hint
                    />
                </v-card-text>

                <v-card-actions class="px-6 pb-5">
                    <v-spacer />
                    <v-btn
                        variant="text"
                        :disabled="submitting"
                        @click="closeFormDialog"
                    >
                        Cancelar
                    </v-btn>
                    <v-btn
                        color="primary"
                        type="submit"
                        :loading="submitting"
                        :disabled="submitting"
                    >
                        Agregar
                    </v-btn>
                </v-card-actions>
            </v-form>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useDestinations } from '@/composables/destinations/useDestinations'
import type { VForm } from 'vuetify/components'

const {
    showFormDialog,
    submitting,
    formError,
    openCreateDialog,
    closeFormDialog,
    createDestination,
} = useDestinations()

const formRef = ref<InstanceType<typeof VForm> | null>(null)
const formValid = ref(false)
const email = ref('')

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const rules = {
    required: (v: string) => !!v?.trim() || 'El correo es obligatorio',
    email: (v: string) => EMAIL_PATTERN.test(v?.trim() || '') || 'Ingresa un correo válido',
}

// Resetea el formulario cada vez que se abre (mismo patrón que
// SearchFormDialog: estado compartido de módulo entre header y diálogo).
watch(showFormDialog, (open) => {
    if (!open) return
    email.value = ''
})

async function submit(): Promise<void> {
    const result = await formRef.value?.validate()
    if (!result?.valid) return
    await createDestination({ destination: email.value.trim() })
}
</script>

<style scoped>
.dialog-surface {
    background: rgb(var(--v-theme-surface-elevated));
}
</style>
