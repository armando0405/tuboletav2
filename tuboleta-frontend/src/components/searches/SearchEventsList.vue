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
                <th>Cambios</th>
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
                <td>
                    <v-btn
                        v-if="event.changesCount > 0"
                        size="small"
                        variant="tonal"
                        color="warning"
                        prepend-icon="mdi-history"
                        @click="openHistory(event)"
                    >
                        {{ event.changesCount }}
                    </v-btn>
                    <span
                        v-else
                        class="text-medium-emphasis"
                    >
                        —
                    </span>
                </td>
            </tr>
        </tbody>
    </v-table>

    <v-dialog
        v-model="showHistory"
        max-width="640"
        @update:model-value="(v) => !v && closeHistory()"
    >
        <v-card>
            <v-card-title class="d-flex align-center ga-2 pt-5 px-6">
                <v-icon
                    icon="mdi-history"
                    color="warning"
                />
                <div>
                    <div class="text-h6 font-weight-bold">Historial de cambios</div>
                    <div class="text-caption text-medium-emphasis">{{ historyEvent?.title }}</div>
                </div>
            </v-card-title>
            <v-card-text class="px-6 position-relative">
                <v-loading :active="historyLoading" />
                <template v-if="!historyLoading">
                    <p
                        v-if="changes.length === 0"
                        class="text-medium-emphasis text-center py-6 mb-0"
                    >
                        Este evento no tiene cambios registrados.
                    </p>
                    <div
                        v-else
                        class="d-flex flex-column ga-4"
                    >
                        <div
                            v-for="c in changes"
                            :key="c.id"
                            class="change-item"
                        >
                            <div class="d-flex align-center justify-space-between ga-2 flex-wrap mb-2">
                                <v-chip
                                    size="small"
                                    color="warning"
                                    variant="tonal"
                                    prepend-icon="mdi-pencil-outline"
                                >
                                    {{ c.fieldLabel }}
                                </v-chip>
                                <span class="text-caption text-medium-emphasis font-mono">
                                    {{ formatDateTime(c.detectedAt) }}
                                </span>
                            </div>
                            <div class="change-diff">
                                <span class="old">{{ c.oldValue || '(vacío)' }}</span>
                                <v-icon
                                    icon="mdi-arrow-right"
                                    size="16"
                                    class="mx-2 diff-arrow"
                                />
                                <span class="new">{{ c.newValue || '(vacío)' }}</span>
                            </div>
                        </div>
                    </div>
                </template>
            </v-card-text>
            <v-card-actions class="px-6 pb-4">
                <v-spacer />
                <v-btn
                    variant="text"
                    @click="closeHistory"
                >
                    Cerrar
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>

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

const {
    events,
    highlightFor,
    sourceUrl,
    showHistory,
    historyEvent,
    changes,
    historyLoading,
    openHistory,
    closeHistory,
} = useSearchEvents()

function formatDateTime(iso: string): string {
    const d = new Date(iso)
    return d.toLocaleString('es-CO', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })
}

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

.change-item {
    background: rgb(var(--v-theme-surface));
    border: 1px solid rgb(var(--v-theme-outline));
    border-radius: 10px;
    padding: 12px 14px;
}

.change-diff {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 2px;
    font-size: 0.9rem;
}

.change-diff .old {
    color: rgb(var(--v-theme-error));
    text-decoration: line-through;
    opacity: 0.85;
}

.change-diff .new {
    color: rgb(var(--v-theme-success));
    font-weight: 600;
}

.change-diff .diff-arrow {
    color: rgb(var(--v-theme-on-surface-variant));
}
</style>
