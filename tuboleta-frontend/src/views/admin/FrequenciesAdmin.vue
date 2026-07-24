<template>
    <v-col
        cols="12"
        class="position-relative"
    >
        <v-loading :active="loading && frequencies.length === 0" />

        <div class="d-flex align-center justify-space-between flex-wrap ga-3 mb-2">
            <div class="d-flex align-center ga-3">
                <v-icon
                    icon="mdi-timer-cog-outline"
                    color="primary"
                    size="28"
                />
                <div>
                    <div class="d-flex align-center ga-2">
                        <h1 class="text-h5 font-weight-bold mb-0">Frecuencias</h1>
                        <v-chip
                            size="x-small"
                            color="primary"
                            variant="tonal"
                        >
                            Solo administrador
                        </v-chip>
                    </div>
                    <p class="text-body-2 text-medium-emphasis mb-0">
                        Catálogo de intervalos (en minutos) que los usuarios pueden elegir al crear
                        una búsqueda.
                    </p>
                </div>
            </div>
            <v-btn
                color="primary"
                prepend-icon="mdi-plus"
                @click="openCreate"
            >
                Nueva frecuencia
            </v-btn>
        </div>

        <v-card
            rounded="lg"
            class="mt-2"
        >
            <v-table>
                <thead>
                    <tr>
                        <th>Etiqueta</th>
                        <th>Minutos</th>
                        <th>Intervalo</th>
                        <th>Estado</th>
                        <th class="text-end">Acción</th>
                    </tr>
                </thead>
                <tbody>
                    <tr
                        v-for="frequency in frequencies"
                        :key="frequency.id"
                    >
                        <td class="font-weight-medium">{{ frequency.label }}</td>
                        <td class="font-mono">{{ frequency.minutes }}</td>
                        <td class="text-medium-emphasis">{{ formatFrequency(frequency.minutes) }}</td>
                        <td>
                            <v-chip
                                :color="frequency.isActive ? 'success' : 'grey'"
                                size="small"
                                variant="tonal"
                                :prepend-icon="
                                    frequency.isActive
                                        ? 'mdi-check-circle-outline'
                                        : 'mdi-pause-circle-outline'
                                "
                            >
                                {{ frequency.isActive ? 'Activa' : 'Inactiva' }}
                            </v-chip>
                        </td>
                        <td class="text-end">
                            <v-btn
                                icon
                                size="small"
                                variant="text"
                                aria-label="Editar frecuencia"
                                @click="openEdit(frequency)"
                            >
                                <v-icon
                                    icon="mdi-circle-edit-outline"
                                    color="success"
                                />
                            </v-btn>
                            <v-btn
                                icon
                                size="small"
                                variant="text"
                                aria-label="Eliminar frecuencia"
                                @click="remove(frequency)"
                            >
                                <v-icon
                                    icon="mdi-delete-outline"
                                    color="error"
                                />
                            </v-btn>
                        </td>
                    </tr>
                    <tr v-if="!loading && frequencies.length === 0">
                        <td
                            colspan="5"
                            class="text-center text-medium-emphasis py-8"
                        >
                            No hay frecuencias en el catálogo. Crea la primera.
                        </td>
                    </tr>
                </tbody>
            </v-table>
        </v-card>

        <v-dialog
            v-model="showFormDialog"
            max-width="480"
            @update:model-value="(val) => !val && closeDialog()"
        >
            <v-card>
                <v-card-title class="d-flex align-center ga-2 pt-5 px-6">
                    <v-icon
                        :icon="editing ? 'mdi-pencil-outline' : 'mdi-plus-circle-outline'"
                        color="primary"
                    />
                    <span class="text-h6 font-weight-bold">
                        {{ editing ? 'Editar frecuencia' : 'Nueva frecuencia' }}
                    </span>
                </v-card-title>

                <v-form
                    ref="formRef"
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
                            v-model="form.label"
                            label="Etiqueta"
                            placeholder="Ej: Cada 15 minutos"
                            :rules="[rules.required]"
                            class="mb-2"
                        />
                        <v-text-field
                            v-model.number="form.minutes"
                            label="Minutos"
                            type="number"
                            min="1"
                            :rules="[rules.positive]"
                            hint="Cada cuántos minutos se monitorea"
                            persistent-hint
                            class="mb-2"
                        />
                        <v-switch
                            v-model="form.isActive"
                            color="primary"
                            :label="form.isActive ? 'Activa (visible para usuarios)' : 'Inactiva'"
                            hide-details
                        />
                    </v-card-text>

                    <v-card-actions class="px-6 pb-5">
                        <v-spacer />
                        <v-btn
                            variant="text"
                            :disabled="submitting"
                            @click="closeDialog"
                        >
                            Cancelar
                        </v-btn>
                        <v-btn
                            color="primary"
                            variant="flat"
                            type="submit"
                            :loading="submitting"
                            :disabled="submitting"
                        >
                            {{ editing ? 'Guardar' : 'Crear' }}
                        </v-btn>
                    </v-card-actions>
                </v-form>
            </v-card>
        </v-dialog>
    </v-col>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useFrequenciesAdmin } from '@/composables/admin/useFrequenciesAdmin'
import { formatFrequency } from '@/utils/text/formatFrequency'
import type { VForm } from 'vuetify/components'
import type { FrequencyRequest } from '@/types/services/Frequency'

const {
    loading,
    frequencies,
    showFormDialog,
    editing,
    submitting,
    formError,
    getFrequencies,
    openCreate,
    openEdit,
    closeDialog,
    save,
    remove,
} = useFrequenciesAdmin()

const formRef = ref<InstanceType<typeof VForm> | null>(null)
const form = ref<{ label: string; minutes: number | null; isActive: boolean }>({
    label: '',
    minutes: null,
    isActive: true,
})

const rules = {
    required: (v: string) => !!v?.trim() || 'La etiqueta es obligatoria',
    positive: (v: number) => (!!v && v > 0) || 'Los minutos deben ser mayores a 0',
}

watch(showFormDialog, (open) => {
    if (!open) return
    if (editing.value) {
        form.value = {
            label: editing.value.label,
            minutes: editing.value.minutes,
            isActive: editing.value.isActive,
        }
    } else {
        form.value = { label: '', minutes: null, isActive: true }
    }
})

async function submit(): Promise<void> {
    const result = await formRef.value?.validate()
    if (!result?.valid || form.value.minutes == null) return
    const payload: FrequencyRequest = {
        label: form.value.label.trim(),
        minutes: form.value.minutes,
        isActive: form.value.isActive,
    }
    await save(payload)
}

onMounted(() => {
    getFrequencies()
})
</script>
