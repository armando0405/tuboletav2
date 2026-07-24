<template>
    <div class="d-flex flex-wrap align-center justify-space-between ga-4 mb-6">
        <div>
            <h1 class="text-h4 font-weight-bold mb-1">Mis búsquedas</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
                Términos que monitoreamos por ti, con su estado por proveedor.
            </p>
        </div>

        <v-btn
            color="primary"
            prepend-icon="mdi-plus"
            @click="openCreateDialog"
        >
            Nueva búsqueda
        </v-btn>
    </div>

    <search-form-dialog />
</template>

<script setup lang="ts">
import SearchFormDialog from '@/components/searches/SearchFormDialog.vue'
import { useSearches } from '@/composables/searches/useSearches'

const { openCreateDialog } = useSearches()
</script>
