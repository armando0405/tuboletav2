<template>
    <div class="d-flex flex-wrap align-center justify-space-between ga-4 mb-6">
        <div>
            <div class="d-flex align-center ga-3">
                <h1 class="text-h4 font-weight-bold mb-1">Notificaciones</h1>
                <v-chip
                    v-if="unreadCount > 0"
                    color="error"
                    variant="flat"
                    size="small"
                    class="font-mono"
                >
                    {{ unreadCount }} sin leer
                </v-chip>
            </div>
            <p class="text-body-2 text-medium-emphasis mb-0">
                Histórico de novedades de tus búsquedas: eventos nuevos, cambiados, eliminados y
                avisos de fuentes deshabilitadas.
            </p>
        </div>

        <div class="d-flex align-center ga-4 flex-wrap">
            <v-switch
                :model-value="unreadOnly"
                label="Solo no leídas"
                color="primary"
                density="compact"
                hide-details
                @update:model-value="(val) => setUnreadOnly(!!val)"
            />
            <v-btn
                variant="outlined"
                color="primary"
                prepend-icon="mdi-check-all"
                :disabled="unreadCount === 0"
                :loading="markingAllRead"
                @click="markAllRead"
            >
                Marcar todas como leídas
            </v-btn>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useNotifications } from '@/composables/notifications/useNotifications'

const { unreadOnly, unreadCount, markingAllRead, setUnreadOnly, markAllRead } = useNotifications()
</script>
