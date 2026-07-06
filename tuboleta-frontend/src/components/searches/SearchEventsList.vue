<template>
    <v-table
        density="comfortable"
        class="events-table"
        hover
    >
        <thead>
            <tr>
                <th>Evento</th>
                <th>Proveedor</th>
                <th>Venue</th>
                <th>Fecha</th>
                <th>Estado</th>
            </tr>
        </thead>
        <tbody>
            <tr
                v-for="event in events"
                :key="event.id"
                :class="rowClass(event)"
            >
                <td>
                    <div class="d-flex align-center ga-2 flex-wrap py-2">
                        <v-chip
                            v-if="highlightFor(event)"
                            size="x-small"
                            :color="highlightColor(highlightFor(event))"
                            variant="flat"
                        >
                            {{ highlightLabel(highlightFor(event)) }}
                        </v-chip>

                        <a
                            v-if="sourceUrl(event)"
                            :href="sourceUrl(event) as string"
                            target="_blank"
                            rel="noopener noreferrer"
                            class="event-title-link"
                        >
                            {{ event.title }}
                            <v-icon
                                icon="mdi-open-in-new"
                                size="14"
                            />
                        </a>
                        <span v-else>{{ event.title }}</span>
                    </div>
                </td>
                <td>{{ event.providerName }}</td>
                <td>{{ event.venue || '—' }}</td>
                <td class="font-mono">{{ formatDate(event) }}</td>
                <td>
                    <v-chip
                        size="small"
                        :color="event.status === 'REMOVED' ? 'error' : 'success'"
                        variant="tonal"
                        :prepend-icon="
                            event.status === 'REMOVED'
                                ? 'mdi-close-circle-outline'
                                : 'mdi-check-circle-outline'
                        "
                    >
                        {{ event.status === 'REMOVED' ? 'Eliminado' : 'Activo' }}
                    </v-chip>
                </td>
            </tr>
        </tbody>
    </v-table>

    <p
        v-if="events.length === 0"
        class="text-body-2 text-medium-emphasis text-center pa-8 mb-0"
    >
        Todavía no se detectaron eventos para esta búsqueda.
    </p>
</template>

<script setup lang="ts">
import { useSearchEvents } from '@/composables/searches/useSearchEvents'
import type { Event } from '@/types/services/Event'
import type { NotificationType } from '@/types/services/Notification'

const { events, highlightFor, sourceUrl } = useSearchEvents()

function rowClass(event: Event): Record<string, boolean> {
    const highlight = highlightFor(event)
    return {
        'row-removed': event.status === 'REMOVED',
        'row-highlight-new': highlight === 'NEW',
        'row-highlight-changed': highlight === 'CHANGED',
        'row-highlight-removed-notice': highlight === 'REMOVED',
    }
}

function highlightColor(type: NotificationType | null): string {
    if (type === 'NEW') return 'success'
    if (type === 'CHANGED') return 'warning'
    if (type === 'REMOVED') return 'error'
    return 'primary'
}

function highlightLabel(type: NotificationType | null): string {
    if (type === 'NEW') return 'NUEVO'
    if (type === 'CHANGED') return 'CAMBIÓ'
    if (type === 'REMOVED') return 'ELIMINADO'
    return ''
}

function formatDate(event: Event): string {
    if (event.eventDate) {
        const [year, month, day] = event.eventDate.split('-').map(Number)
        return new Date(year, month - 1, day).toLocaleDateString('es-CO', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        })
    }
    return event.eventDateRaw || '—'
}
</script>

<style scoped>
.events-table :deep(tbody tr) {
    border-left: 3px solid transparent;
}

.events-table :deep(tr.row-removed) {
    opacity: 0.6;
}

.events-table :deep(tr.row-highlight-new) {
    border-left-color: rgb(var(--v-theme-success));
    background: rgba(var(--v-theme-success), 0.06);
}

.events-table :deep(tr.row-highlight-changed) {
    border-left-color: rgb(var(--v-theme-warning));
    background: rgba(var(--v-theme-warning), 0.06);
}

.events-table :deep(tr.row-highlight-removed-notice) {
    border-left-color: rgb(var(--v-theme-error));
    background: rgba(var(--v-theme-error), 0.06);
}

.event-title-link {
    color: rgb(var(--v-theme-on-surface));
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.event-title-link:hover {
    color: rgb(var(--v-theme-primary));
    text-decoration: underline;
}
</style>
