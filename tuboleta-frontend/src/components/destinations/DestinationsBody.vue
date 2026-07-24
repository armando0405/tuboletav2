<template>
    <v-table
        density="comfortable"
        class="destinations-table"
        hover
    >
        <thead>
            <tr>
                <th>Canal</th>
                <th>Destino</th>
                <th>Estado</th>
                <th class="text-right">Acción</th>
            </tr>
        </thead>
        <tbody>
            <tr
                v-for="destination in destinations"
                :key="destination.id"
            >
                <td>
                    <v-chip
                        variant="outlined"
                        size="small"
                        prepend-icon="mdi-email-outline"
                    >
                        {{ destination.channelName }}
                    </v-chip>
                </td>
                <td class="font-mono">{{ destination.destination }}</td>
                <td>
                    <v-chip
                        size="small"
                        variant="tonal"
                        :color="destination.isActive ? 'success' : 'warning'"
                        :prepend-icon="
                            destination.isActive
                                ? 'mdi-check-circle-outline'
                                : 'mdi-pause-circle-outline'
                        "
                    >
                        {{ destination.isActive ? 'Activo' : 'Inactivo' }}
                    </v-chip>
                </td>
                <td class="text-right">
                    <v-btn
                        size="small"
                        :variant="destination.isActive ? 'outlined' : 'flat'"
                        :color="destination.isActive ? 'warning' : 'primary'"
                        :prepend-icon="destination.isActive ? 'mdi-pause' : 'mdi-play'"
                        @click="toggleDestination(destination)"
                    >
                        {{ destination.isActive ? 'Desactivar' : 'Activar' }}
                    </v-btn>
                </td>
            </tr>
        </tbody>
    </v-table>

    <div
        v-if="!loading && destinations.length === 0"
        class="pa-8 text-center"
    >
        <v-icon
            icon="mdi-email-off-outline"
            size="40"
            color="primary"
            class="mb-3"
        />
        <p class="text-body-1 font-weight-medium mb-1">Todavía no tienes destinos</p>
        <p class="text-body-2 text-medium-emphasis mb-0">
            Agrega un correo para poder elegirlo al crear o editar una búsqueda.
        </p>
    </div>
</template>

<script setup lang="ts">
import { useDestinations } from '@/composables/destinations/useDestinations'

const { loading, destinations, toggleDestination } = useDestinations()
</script>

<style scoped>
.destinations-table :deep(tbody tr) {
    border-left: 3px solid transparent;
}
</style>
