<template>
    <v-col
        cols="12"
        class="position-relative"
    >
        <v-loading :active="loading && destinations.length === 0" />

        <destinations-header />

        <v-card
            variant="flat"
            class="destinations-card"
        >
            <destinations-body />
        </v-card>
    </v-col>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import DestinationsHeader from '@/components/destinations/DestinationsHeader.vue'
import DestinationsBody from '@/components/destinations/DestinationsBody.vue'
import { useDestinations } from '@/composables/destinations/useDestinations'

const { loading, destinations, getDestinations } = useDestinations()

onMounted(() => {
    getDestinations()
})
</script>

<style scoped>
.destinations-card {
    background: rgb(var(--v-theme-surface));
    border: 1px solid rgb(var(--v-theme-outline));
    overflow-x: auto;
}
</style>
