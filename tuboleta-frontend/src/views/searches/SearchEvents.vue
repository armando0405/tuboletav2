<template>
    <v-col
        cols="12"
        class="position-relative"
    >
        <v-loading :active="loading" />

        <search-events-header />

        <v-card
            variant="flat"
            class="events-card"
        >
            <search-events-list />
        </v-card>
    </v-col>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import SearchEventsHeader from '@/components/searches/SearchEventsHeader.vue'
import SearchEventsList from '@/components/searches/SearchEventsList.vue'
import { useSearchEvents } from '@/composables/searches/useSearchEvents'

const route = useRoute()
const { loading, getEvents } = useSearchEvents()

const searchId = computed<number>(() => Number(route.params.id))

onMounted(() => {
    getEvents(searchId.value)
})
</script>

<style scoped>
.events-card {
    background: rgb(var(--v-theme-surface));
    border: 1px solid rgb(var(--v-theme-outline));
    overflow-x: auto;
}
</style>
