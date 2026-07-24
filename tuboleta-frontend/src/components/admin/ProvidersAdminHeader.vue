<template>
    <div class="mb-6 d-flex justify-space-between align-start ga-4 flex-wrap">
        <div>
            <div class="d-flex align-center ga-2 mb-1">
                <v-icon
                    icon="mdi-shield-crown-outline"
                    color="primary"
                />
                <h1 class="text-h4 font-weight-bold mb-0">Fuentes</h1>
                <v-chip
                    size="small"
                    variant="tonal"
                    color="primary"
                >
                    Solo administrador
                </v-chip>
            </div>
            <p class="text-body-2 text-medium-emphasis mb-0">
                Registra, edita, habilita o deshabilita proveedores. Al deshabilitar una fuente, el
                scheduler deja de monitorearla y se notifica a los usuarios con búsquedas afectadas.
            </p>
        </div>
        <v-btn
            color="primary"
            variant="flat"
            prepend-icon="mdi-database-plus-outline"
            @click="openCreateForm"
        >
            Nueva fuente
        </v-btn>
    </div>
</template>

<script setup lang="ts">
import { useProvidersAdmin } from '@/composables/admin/useProvidersAdmin'

const { openCreateForm } = useProvidersAdmin()
</script>
