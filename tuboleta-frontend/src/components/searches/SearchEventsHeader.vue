<template>
    <div class="d-flex align-center ga-3 mb-2">
        <v-btn
            icon
            variant="text"
            aria-label="Volver a mis búsquedas"
            @click="goBack"
        >
            <v-icon icon="mdi-arrow-left" />
        </v-btn>
        <div>
            <p class="text-caption text-medium-emphasis mb-0">Eventos de</p>
            <h1 class="text-h5 font-weight-bold mb-0">{{ search?.term || 'Búsqueda' }}</h1>
        </div>
    </div>

    <div
        v-if="search"
        class="d-flex flex-wrap ga-2 mb-6"
    >
        <v-chip
            variant="outlined"
            size="small"
            prepend-icon="mdi-timer-outline"
            class="font-mono"
        >
            Cada {{ search.checkFrequencyHours }}h
        </v-chip>
        <v-chip
            variant="outlined"
            size="small"
            prepend-icon="mdi-format-list-bulleted"
        >
            {{ events.length }} evento(s)
        </v-chip>
    </div>
</template>

<script setup lang="ts">
import { useSearchEvents } from '@/composables/searches/useSearchEvents'
import { router } from '@/router'

const { search, events } = useSearchEvents()

function goBack(): void {
    router.push({ name: 'searches' })
}
</script>
