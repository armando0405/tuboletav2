<template>
    <v-dialog
        v-model="showFormDialog"
        max-width="560"
        @update:model-value="(val) => !val && closeFormDialog()"
    >
        <v-card class="dialog-surface">
            <v-card-title class="d-flex align-center ga-2 pt-5 px-6">
                <v-icon
                    :icon="isEdit ? 'mdi-pencil-outline' : 'mdi-plus-circle-outline'"
                    color="primary"
                />
                <span class="text-h6 font-weight-bold">
                    {{ isEdit ? 'Editar búsqueda' : 'Nueva búsqueda' }}
                </span>
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

                    <template v-if="isEdit">
                        <v-text-field
                            :model-value="editingSearch?.term"
                            label="Término"
                            readonly
                            disabled
                            hint="El término no se puede editar; elimina la búsqueda y crea una nueva si necesitas cambiarlo."
                            persistent-hint
                            class="mb-4"
                        />
                    </template>
                    <template v-else>
                        <v-text-field
                            v-model="form.term"
                            label="Término a monitorear"
                            placeholder="Ej: Bad Bunny"
                            :rules="[rules.required]"
                            autofocus
                            class="mb-1"
                        />
                        <p
                            v-if="normalizedPreview"
                            class="text-caption text-medium-emphasis mb-4"
                        >
                            Se guardará como
                            <span class="font-mono">{{ normalizedPreview }}</span>
                            ; dos búsquedas equivalentes tras normalizar cuentan como duplicadas.
                        </p>
                    </template>

                    <v-select
                        v-model="form.checkFrequencyMinutes"
                        :items="frequencyItems"
                        label="Frecuencia de monitoreo"
                        :loading="catalogsLoading"
                        :rules="[rules.frequencyRequired]"
                        prepend-inner-icon="mdi-timer-outline"
                        class="mb-5"
                    />

                    <template v-if="!isEdit">
                        <v-select
                            v-model="form.providerIds"
                            :items="providerCatalog"
                            item-title="name"
                            item-value="id"
                            label="Proveedores a monitorear"
                            multiple
                            chips
                            closable-chips
                            :loading="catalogsLoading"
                            :rules="[rules.atLeastOneProvider]"
                            hint="Al menos uno de los proveedores del catálogo activo"
                            persistent-hint
                            class="mb-5"
                        />
                    </template>

                    <v-select
                        v-model="form.destinationIds"
                        :items="destinationItems"
                        label="Destinos de notificación"
                        multiple
                        chips
                        closable-chips
                        :loading="catalogsLoading"
                        hint="Opcional: a quién avisar cuando haya novedades"
                        persistent-hint
                    />
                    <p
                        v-if="!catalogsLoading && destinationCatalog.length === 0"
                        class="text-caption text-medium-emphasis mt-2"
                    >
                        No tienes destinos configurados todavía.
                        <router-link
                            to="/destinos"
                            class="link"
                            @click="closeFormDialog"
                        >
                            Crear uno
                        </router-link>
                    </p>
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
                        variant="flat"
                        type="submit"
                        :loading="submitting"
                        :disabled="submitting"
                    >
                        {{ isEdit ? 'Guardar cambios' : 'Crear búsqueda' }}
                    </v-btn>
                </v-card-actions>
            </v-form>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useSearches } from '@/composables/searches/useSearches'
import { previewNormalizedTerm } from '@/utils/text/normalizeTerm'
import type { VForm } from 'vuetify/components'
import type { SearchCreateRequest, SearchUpdateRequest } from '@/types/services/Search'

const {
    showFormDialog,
    formMode,
    editingSearch,
    submitting,
    formError,
    providerCatalog,
    destinationCatalog,
    frequencyCatalog,
    catalogsLoading,
    closeFormDialog,
    createSearch,
    editSearch,
} = useSearches()

const formRef = ref<InstanceType<typeof VForm> | null>(null)
const formValid = ref(false)

const form = ref<{
    term: string
    checkFrequencyMinutes: number
    providerIds: number[]
    destinationIds: number[]
}>({
    term: '',
    checkFrequencyMinutes: 1440,
    providerIds: [],
    destinationIds: [],
})

// Opciones del select desde el catálogo activo (el value es el nº de minutos).
const frequencyItems = computed(() =>
    frequencyCatalog.value.map((f) => ({ title: f.label, value: f.minutes })),
)

const isEdit = computed<boolean>(() => formMode.value === 'edit')

const normalizedPreview = computed<string>(() => previewNormalizedTerm(form.value.term))

// Solo los destinos ACTIVOS son elegibles al crear/editar una búsqueda: un
// destino inactivo no debe poder seleccionarse como receptor de avisos.
// (La pantalla de Destinos sí muestra activos+inactivos, para gestionarlos.)
const destinationItems = computed(() =>
    destinationCatalog.value
        .filter((d) => d.isActive)
        .map((d) => ({
            title: d.destination,
            value: d.id,
        })),
)

const rules = {
    required: (v: string) => !!v?.trim() || 'El término es obligatorio',
    atLeastOneProvider: (v: number[]) => (v && v.length > 0) || 'Selecciona al menos un proveedor',
    frequencyRequired: (v: number) => !!v || 'Selecciona una frecuencia',
}

// Resetea el formulario cada vez que se abre el diálogo (patrón de estado
// compartido: SearchesHeader/SearchCard disparan openCreateDialog/openEditDialog
// sobre el mismo estado de módulo que este diálogo consume). En edición,
// precarga destinationIds (SearchResponse.destinationIds) para que el
// multi-select arranque con la selección real — ya no hace falta el switch
// "actualizar destinos" que existía porque antes no había forma de saber
// qué destinos estaban asignados.
watch(showFormDialog, (open) => {
    if (!open) return
    if (isEdit.value && editingSearch.value) {
        form.value = {
            term: editingSearch.value.term,
            checkFrequencyMinutes: editingSearch.value.checkFrequencyMinutes,
            providerIds: [],
            destinationIds: [...editingSearch.value.destinationIds],
        }
    } else {
        form.value = { term: '', checkFrequencyMinutes: 1440, providerIds: [], destinationIds: [] }
    }
})

async function submit(): Promise<void> {
    const result = await formRef.value?.validate()
    if (!result?.valid) return

    if (isEdit.value && editingSearch.value) {
        const payload: SearchUpdateRequest = {
            checkFrequencyMinutes: form.value.checkFrequencyMinutes,
            destinationIds: form.value.destinationIds,
        }
        await editSearch(editingSearch.value.id, payload)
        return
    }

    const payload: SearchCreateRequest = {
        term: form.value.term.trim(),
        checkFrequencyMinutes: form.value.checkFrequencyMinutes,
        providerIds: form.value.providerIds,
        destinationIds: form.value.destinationIds,
    }
    await createSearch(payload)
}
</script>

<style scoped>
.dialog-surface {
    background: rgb(var(--v-theme-surface-elevated));
}
</style>
