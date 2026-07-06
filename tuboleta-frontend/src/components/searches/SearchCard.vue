<template>
    <v-card
        variant="flat"
        class="search-card h-100 d-flex flex-column"
    >
        <v-card-item>
            <template #prepend>
                <v-icon
                    icon="mdi-magnify"
                    color="primary"
                />
            </template>

            <v-card-title class="text-h6 font-weight-bold">
                {{ search.term }}
            </v-card-title>

            <template #append>
                <v-tooltip text="Editar frecuencia y destinos">
                    <template #activator="{ props: activatorProps }">
                        <v-btn
                            v-bind="activatorProps"
                            icon
                            size="small"
                            variant="text"
                            aria-label="Editar búsqueda"
                            @click="emit('edit', search)"
                        >
                            <v-icon
                                icon="mdi-circle-edit-outline"
                                color="success"
                            />
                        </v-btn>
                    </template>
                </v-tooltip>

                <v-tooltip text="Eliminar búsqueda">
                    <template #activator="{ props: activatorProps }">
                        <v-btn
                            v-bind="activatorProps"
                            icon
                            size="small"
                            variant="text"
                            aria-label="Eliminar búsqueda"
                            @click="emit('delete', search)"
                        >
                            <v-icon
                                icon="mdi-delete-outline"
                                color="error"
                            />
                        </v-btn>
                    </template>
                </v-tooltip>
            </template>
        </v-card-item>

        <v-card-text class="flex-grow-1">
            <div class="d-flex flex-wrap ga-2 mb-4">
                <v-chip
                    variant="outlined"
                    size="small"
                    prepend-icon="mdi-timer-outline"
                    class="font-mono"
                >
                    Cada {{ search.checkFrequencyHours }}h
                </v-chip>

                <v-chip
                    v-if="search.status !== 'ACTIVE'"
                    variant="tonal"
                    size="small"
                    color="warning"
                    prepend-icon="mdi-pause-circle-outline"
                >
                    Búsqueda pausada
                </v-chip>
            </div>

            <p class="text-caption text-medium-emphasis text-uppercase font-weight-medium mb-2">
                Proveedores
            </p>

            <div class="d-flex flex-wrap ga-2">
                <search-provider-chip
                    v-for="providerInfo in search.providers"
                    :key="providerInfo.providerId"
                    :provider="providerInfo"
                    @toggle="emit('toggle-provider', search, providerInfo.providerId)"
                />

                <span
                    v-if="search.providers.length === 0"
                    class="text-caption text-medium-emphasis"
                >
                    Sin proveedores asignados
                </span>
            </div>
        </v-card-text>

        <v-divider />

        <v-card-actions>
            <v-btn
                variant="text"
                color="primary"
                append-icon="mdi-arrow-right"
                block
                @click="emit('view-events', search)"
            >
                Ver eventos
            </v-btn>
        </v-card-actions>
    </v-card>
</template>

<script setup lang="ts">
import SearchProviderChip from '@/components/searches/SearchProviderChip.vue'
import type { Search } from '@/types/services/Search'

defineProps<{
    search: Search
}>()

const emit = defineEmits<{
    edit: [search: Search]
    delete: [search: Search]
    'view-events': [search: Search]
    'toggle-provider': [search: Search, providerId: number]
}>()
</script>

<style scoped>
.search-card {
    background: rgb(var(--v-theme-surface));
    border: 1px solid rgb(var(--v-theme-outline));
    transition:
        border-color 0.2s ease-out,
        background-color 0.2s ease-out;
}

.search-card:hover {
    background: rgb(var(--v-theme-surface-elevated));
    border-color: rgb(var(--v-theme-primary));
}
</style>
