<template>
    <v-table
        density="comfortable"
        class="providers-table"
        hover
    >
        <thead>
            <tr>
                <th>Fuente</th>
                <th>Tipo</th>
                <th>Estado</th>
                <th>Razón / última actualización</th>
                <th class="text-right">Acción</th>
            </tr>
        </thead>
        <tbody>
            <tr
                v-for="provider in providers"
                :key="provider.id"
                :class="{ 'row-disabled': provider.status === 'DISABLED' }"
            >
                <td>
                    <div class="d-flex flex-column">
                        <span class="font-weight-medium">{{ provider.name }}</span>
                        <span class="text-caption text-medium-emphasis">
                            {{ provider.baseUrl }}
                        </span>
                    </div>
                </td>
                <td>
                    <v-chip
                        variant="outlined"
                        size="small"
                    >
                        {{ provider.providerType }}
                    </v-chip>
                </td>
                <td>
                    <v-chip
                        size="small"
                        variant="tonal"
                        :color="provider.status === 'ACTIVE' ? 'success' : 'error'"
                        :prepend-icon="
                            provider.status === 'ACTIVE'
                                ? 'mdi-check-circle-outline'
                                : 'mdi-alert-circle-outline'
                        "
                    >
                        {{ provider.status === 'ACTIVE' ? 'Activa' : 'Deshabilitada' }}
                    </v-chip>
                </td>
                <td>
                    <div
                        v-if="provider.status === 'DISABLED'"
                        class="d-flex flex-column"
                    >
                        <span class="text-body-2">{{ provider.statusReason || '—' }}</span>
                        <span
                            v-if="provider.statusChangedAt"
                            class="text-caption text-medium-emphasis font-mono"
                        >
                            {{ formatDate(provider.statusChangedAt) }}
                        </span>
                    </div>
                    <span
                        v-else
                        class="text-body-2 text-medium-emphasis"
                    >
                        —
                    </span>
                </td>
                <td class="text-right">
                    <div class="d-flex justify-end ga-2 flex-wrap py-1">
                        <v-btn
                            size="small"
                            variant="tonal"
                            prepend-icon="mdi-pencil-outline"
                            @click="openEditForm(provider)"
                        >
                            Editar
                        </v-btn>
                        <v-btn
                            v-if="provider.status === 'ACTIVE'"
                            size="small"
                            variant="outlined"
                            color="error"
                            prepend-icon="mdi-database-off-outline"
                            @click="openDisableDialog(provider)"
                        >
                            Deshabilitar
                        </v-btn>
                        <v-btn
                            v-else
                            size="small"
                            variant="flat"
                            color="primary"
                            prepend-icon="mdi-database-check-outline"
                            @click="enableProvider(provider)"
                        >
                            Habilitar
                        </v-btn>
                    </div>
                </td>
            </tr>
        </tbody>
    </v-table>

    <div
        v-if="!loading && providers.length === 0"
        class="pa-8 text-center"
    >
        <v-icon
            icon="mdi-database-search-outline"
            size="40"
            color="primary"
            class="mb-3"
        />
        <p class="text-body-1 font-weight-medium mb-0">Todavía no hay fuentes en el catálogo</p>
    </div>
</template>

<script setup lang="ts">
import { useProvidersAdmin } from '@/composables/admin/useProvidersAdmin'

const { loading, providers, openDisableDialog, enableProvider, openEditForm } = useProvidersAdmin()

function formatDate(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })
}
</script>

<style scoped>
.providers-table :deep(tbody tr) {
    border-left: 3px solid transparent;
}

.providers-table :deep(tr.row-disabled) {
    border-left-color: rgb(var(--v-theme-error));
    background: rgba(var(--v-theme-error), 0.05);
}
</style>
