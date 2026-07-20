<template>
    <v-dialog
        v-model="showDisableDialog"
        max-width="520"
        @update:model-value="(val) => !val && closeDisableDialog()"
    >
        <v-card class="dialog-surface">
            <v-card-title class="d-flex align-center ga-2 pt-5 px-6">
                <v-icon
                    icon="mdi-database-off-outline"
                    color="error"
                />
                <span class="text-h6 font-weight-bold">
                    Deshabilitar "{{ disablingProvider?.name }}"
                </span>
            </v-card-title>

            <v-form
                ref="formRef"
                v-model="formValid"
                @submit.prevent="submit"
            >
                <v-card-text class="px-6">
                    <v-alert
                        type="warning"
                        variant="tonal"
                        density="compact"
                        icon="mdi-bell-alert-outline"
                        class="mb-4"
                    >
                        Se notificará de inmediato a todos los usuarios con búsquedas activas sobre
                        esta fuente (aviso "fuente deshabilitada" en su inbox y por correo). El
                        scheduler dejará de ejecutar sus búsquedas de inmediato.
                    </v-alert>

                    <v-textarea
                        v-model="reason"
                        label="Motivo de la deshabilitación"
                        placeholder="Ej: fallas repetidas desde el 05/07, en revisión con el proveedor"
                        :rules="[rules.required]"
                        rows="3"
                        auto-grow
                        autofocus
                        hint="Obligatorio: explica el motivo y, si aplica, el matiz temporal/permanente. Los usuarios afectados serán notificados de que esta fuente fue deshabilitada y verán este motivo en el detalle de sus búsquedas."
                        persistent-hint
                    />
                </v-card-text>

                <v-card-actions class="px-6 pb-5">
                    <v-spacer />
                    <v-btn
                        variant="text"
                        :disabled="submitting"
                        @click="closeDisableDialog"
                    >
                        Cancelar
                    </v-btn>
                    <v-btn
                        color="error"
                        variant="flat"
                        type="submit"
                        :loading="submitting"
                        :disabled="submitting"
                    >
                        Deshabilitar fuente
                    </v-btn>
                </v-card-actions>
            </v-form>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useProvidersAdmin } from '@/composables/admin/useProvidersAdmin'
import type { VForm } from 'vuetify/components'

const { showDisableDialog, disablingProvider, submitting, closeDisableDialog, disableProvider } =
    useProvidersAdmin()

const formRef = ref<InstanceType<typeof VForm> | null>(null)
const formValid = ref(false)
const reason = ref('')

const rules = {
    required: (v: string) => !!v?.trim() || 'La razón es obligatoria',
}

watch(showDisableDialog, (open) => {
    if (!open) return
    reason.value = ''
})

async function submit(): Promise<void> {
    const result = await formRef.value?.validate()
    if (!result?.valid) return
    await disableProvider(reason.value.trim())
}
</script>

<style scoped>
.dialog-surface {
    background: rgb(var(--v-theme-surface-elevated));
}
</style>
