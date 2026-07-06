<template>
    <v-list class="notifications-list py-0">
        <template
            v-for="(item, index) in notifications"
            :key="item.id"
        >
            <v-divider v-if="index > 0" />
            <v-list-item
                class="notification-row py-3 px-4"
                :class="{ 'is-unread': !item.readAt }"
            >
                <template #prepend>
                    <div class="d-flex align-center ga-2">
                        <span
                            v-if="!item.readAt"
                            class="unread-dot"
                            aria-hidden="true"
                        />
                        <v-avatar
                            :color="typeInfo(item.type).color"
                            variant="tonal"
                            size="40"
                        >
                            <v-icon
                                :icon="typeInfo(item.type).icon"
                                :color="typeInfo(item.type).color"
                            />
                        </v-avatar>
                    </div>
                </template>

                <div class="d-flex flex-column ga-1">
                    <div class="d-flex align-center ga-2 flex-wrap">
                        <v-chip
                            :color="typeInfo(item.type).color"
                            variant="tonal"
                            size="x-small"
                            class="font-weight-medium"
                        >
                            {{ typeInfo(item.type).label }}
                        </v-chip>
                        <span class="text-caption text-medium-emphasis">{{ item.searchTerm }}</span>
                    </div>
                    <p
                        class="text-body-2 mb-0"
                        :class="item.readAt ? 'text-medium-emphasis' : 'font-weight-medium'"
                    >
                        {{ bodyText(item) }}
                    </p>
                    <span class="text-caption text-medium-emphasis font-mono">
                        {{ formatDate(item.createdAt) }}
                    </span>
                </div>

                <template #append>
                    <v-btn
                        v-if="!item.readAt"
                        size="small"
                        variant="text"
                        color="primary"
                        prepend-icon="mdi-check"
                        @click="markRead(item)"
                    >
                        Marcar leída
                    </v-btn>
                    <v-chip
                        v-else
                        size="small"
                        variant="text"
                        class="text-medium-emphasis"
                        prepend-icon="mdi-check-circle-outline"
                    >
                        Leída
                    </v-chip>
                </template>
            </v-list-item>
        </template>
    </v-list>

    <div
        v-if="!loading && notifications.length === 0"
        class="pa-8 text-center"
    >
        <v-icon
            icon="mdi-bell-check-outline"
            size="40"
            color="primary"
            class="mb-3"
        />
        <p class="text-body-1 font-weight-medium mb-1">
            {{
                unreadOnly
                    ? 'No tienes notificaciones sin leer'
                    : 'Todavía no tienes notificaciones'
            }}
        </p>
        <p class="text-body-2 text-medium-emphasis mb-0">
            Aquí verás los eventos nuevos, cambiados o eliminados de tus búsquedas.
        </p>
    </div>
</template>

<script setup lang="ts">
import { useNotifications } from '@/composables/notifications/useNotifications'
import type { Notification, NotificationType } from '@/types/services/Notification'

const { loading, notifications, unreadOnly, markRead } = useNotifications()

type TypeInfo = {
    label: string
    color: 'success' | 'warning' | 'error'
    icon: string
}

function typeInfo(type: NotificationType): TypeInfo {
    switch (type) {
        case 'NEW':
            return { label: 'NUEVO', color: 'success', icon: 'mdi-bell-plus-outline' }
        case 'CHANGED':
            return { label: 'CAMBIÓ', color: 'warning', icon: 'mdi-pencil-outline' }
        case 'REMOVED':
            return { label: 'ELIMINADO', color: 'error', icon: 'mdi-close-circle-outline' }
        case 'PROVIDER_DISABLED':
            return {
                label: 'FUENTE DESHABILITADA',
                color: 'error',
                icon: 'mdi-database-off-outline',
            }
        default:
            return { label: type, color: 'warning', icon: 'mdi-bell-outline' }
    }
}

// PROVIDER_DISABLED no trae eventTitle (no aplica: no hay evento asociado,
// REQ-FUE-002) — el backend tampoco expone aquí el `status_reason` de la
// fuente (solo en /api/admin/providers, y solo para ADMIN), así que el
// cuerpo de este tipo es un texto fijo con el término afectado.
function bodyText(item: Notification): string {
    if (item.eventTitle) return item.eventTitle
    if (item.type === 'PROVIDER_DISABLED') {
        return `Una fuente de "${item.searchTerm}" fue deshabilitada por el administrador.`
    }
    return 'Sin detalle adicional.'
}

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
.notifications-list {
    background: transparent;
}

.notification-row {
    border-left: 3px solid transparent;
    transition: background-color 0.2s ease-out;
}

.notification-row.is-unread {
    border-left-color: rgb(var(--v-theme-primary));
    background: rgba(var(--v-theme-primary), 0.06);
}

.unread-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgb(var(--v-theme-primary));
    flex-shrink: 0;
}
</style>
