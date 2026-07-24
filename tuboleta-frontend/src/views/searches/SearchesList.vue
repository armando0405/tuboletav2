<template>
    <v-col
        cols="12"
        class="position-relative"
    >
        <v-loading :active="loading && searches.length === 0" />

        <searches-header />

        <v-row v-if="searches.length > 0">
            <v-col
                v-for="search in searches"
                :key="search.id"
                cols="12"
                sm="6"
                lg="4"
            >
                <search-card
                    :search="search"
                    :unread-count="unreadCountFor(search)"
                    :running="runningId === search.id"
                    @edit="openEditDialog"
                    @delete="deleteSearch"
                    @view-events="goToEvents"
                    @toggle-provider="togglePair"
                    @toggle-status="toggleStatus"
                    @run-now="runNow"
                />
            </v-col>
        </v-row>

        <v-card
            v-else-if="!loading"
            variant="tonal"
            class="pa-8 text-center"
        >
            <v-icon
                icon="mdi-magnify"
                size="48"
                color="primary"
                class="mb-3"
            />
            <p class="text-h6 font-weight-bold mb-1">Todavía no tienes búsquedas</p>
            <p class="text-body-2 text-medium-emphasis mb-4">
                Crea una para empezar a monitorear un término en tus proveedores.
            </p>
            <v-btn
                color="primary"
                prepend-icon="mdi-plus"
                @click="openCreateDialog"
            >
                Nueva búsqueda
            </v-btn>
        </v-card>
    </v-col>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import SearchesHeader from '@/components/searches/SearchesHeader.vue'
import SearchCard from '@/components/searches/SearchCard.vue'
import { useSearches } from '@/composables/searches/useSearches'
import { router } from '@/router'
import type { Search } from '@/types/services/Search'

const {
    loading,
    searches,
    runningId,
    getSearches,
    loadUnreadBadges,
    unreadCountFor,
    openCreateDialog,
    openEditDialog,
    togglePair,
    toggleStatus,
    runNow,
    deleteSearch,
} = useSearches()

onMounted(() => {
    getSearches()
    // Best-effort, no bloquea el listado si falla (ver useSearches.ts).
    loadUnreadBadges()
})

function goToEvents(search: Search): void {
    router.push({ name: 'search-events', params: { id: search.id } })
}
</script>
