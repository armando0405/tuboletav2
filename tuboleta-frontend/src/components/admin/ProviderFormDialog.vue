<template>
    <v-dialog
        v-model="showFormDialog"
        max-width="620"
        persistent
    >
        <v-card rounded="lg">
            <v-card-title class="d-flex align-center ga-2 pt-5 px-6">
                <v-icon :icon="isEdit ? 'mdi-database-edit-outline' : 'mdi-database-plus-outline'" />
                <span class="text-h6 font-weight-bold">
                    {{ isEdit ? 'Editar fuente' : 'Nueva fuente' }}
                </span>
            </v-card-title>

            <v-card-text class="px-6">
                <v-text-field
                    label="Nombre"
                    v-model="form.name"
                    :disabled="savingForm"
                />
                <v-select
                    label="Tipo"
                    v-model="form.providerType"
                    :items="typeItems"
                    :disabled="savingForm"
                    hint="SCRAPER = se lee el HTML · API = se consume una API JSON"
                    persistent-hint
                    class="mt-1"
                />
                <v-text-field
                    label="URL base"
                    v-model="form.baseUrl"
                    placeholder="https://www.ejemplo.com"
                    :disabled="savingForm"
                    class="mt-3"
                />
                <v-text-field
                    label="URL de búsqueda (usa {term})"
                    v-model="form.searchUrl"
                    placeholder="https://www.ejemplo.com/buscar?q={term}"
                    :disabled="savingForm"
                    :error-messages="searchUrlError"
                    class="mt-1"
                />
                <v-textarea
                    label="Config (JSON: selectores o rutas)"
                    v-model="form.config"
                    :disabled="savingForm"
                    :error-messages="configError"
                    rows="5"
                    class="mt-1 font-mono"
                    placeholder='{"item_selector":".event","title_selector":".t","link_selector":"a"}'
                />
                <p class="text-caption text-medium-emphasis mb-0">
                    {{ configHint }}
                </p>
            </v-card-text>

            <v-card-actions class="px-6 pb-4">
                <v-spacer />
                <v-btn
                    variant="text"
                    :disabled="savingForm"
                    @click="closeFormDialog"
                >
                    Cancelar
                </v-btn>
                <v-btn
                    color="primary"
                    variant="flat"
                    :loading="savingForm"
                    :disabled="!canSubmit"
                    @click="submit"
                >
                    {{ isEdit ? 'Guardar cambios' : 'Crear fuente' }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useProvidersAdmin } from '@/composables/admin/useProvidersAdmin'
import type { ProviderType } from '@/types/services/Provider'

const { showFormDialog, editingProvider, savingForm, closeFormDialog, saveProvider } =
    useProvidersAdmin()

const typeItems = ['SCRAPER', 'API']

const form = reactive({
    name: '',
    providerType: 'SCRAPER' as ProviderType,
    baseUrl: '',
    searchUrl: '',
    config: '',
})

const isEdit = computed<boolean>(() => editingProvider.value !== null)

// (Re)inicializa el formulario cada vez que se abre el diálogo.
watch(showFormDialog, (open) => {
    if (!open) return
    const p = editingProvider.value
    form.name = p?.name ?? ''
    form.providerType = p?.providerType ?? 'SCRAPER'
    form.baseUrl = p?.baseUrl ?? ''
    form.searchUrl = p?.searchUrl ?? ''
    form.config = p?.config ?? ''
})

const searchUrlError = computed<string[]>(() =>
    form.searchUrl && !form.searchUrl.includes('{term}')
        ? ['Debe incluir {term} (se reemplaza por el término buscado)']
        : [],
)

const configError = computed<string[]>(() => {
    if (!form.config.trim()) return []
    try {
        JSON.parse(form.config)
        return []
    } catch {
        return ['El config no es un JSON válido']
    }
})

const configHint = computed<string>(() =>
    form.providerType === 'API'
        ? 'API: {"items_path":"data.events","id_field":"id","title_field":"name",...}'
        : 'SCRAPER: {"item_selector":".event","title_selector":".t","link_selector":"a",...}',
)

const canSubmit = computed<boolean>(
    () =>
        !!form.name &&
        !!form.baseUrl &&
        !!form.searchUrl &&
        searchUrlError.value.length === 0 &&
        configError.value.length === 0,
)

const submit = async () => {
    if (!canSubmit.value) return
    await saveProvider({
        name: form.name,
        providerType: form.providerType,
        baseUrl: form.baseUrl,
        searchUrl: form.searchUrl,
        config: form.config.trim() ? form.config : null,
    })
}
</script>
