<template>
    <v-col
        cols="12"
        class="position-relative"
    >
        <v-loading :active="loading && notifications.length === 0" />

        <notifications-header />

        <v-card
            variant="flat"
            class="inbox-card"
        >
            <notifications-list />
        </v-card>
    </v-col>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import NotificationsHeader from '@/components/notifications/NotificationsHeader.vue'
import NotificationsList from '@/components/notifications/NotificationsList.vue'
import { useNotifications } from '@/composables/notifications/useNotifications'

const { loading, notifications, getNotifications, getUnreadCount } = useNotifications()

onMounted(() => {
    // NUNCA marca nada como leído por el solo hecho de listar (REQ-NOT-003);
    // solo carga el histórico + refresca el contador (por si cambió en otra
    // pestaña/sesión).
    getNotifications()
    getUnreadCount()
})
</script>

<style scoped>
.inbox-card {
    background: rgb(var(--v-theme-surface));
    border: 1px solid rgb(var(--v-theme-outline));
    overflow-x: auto;
}
</style>
