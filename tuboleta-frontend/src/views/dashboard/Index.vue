<template>
    <div class="pa-2">
        <div class="mb-6">
            <h1 class="text-h4 font-weight-bold">Hola, {{ userName }}</h1>
            <p class="text-medium-emphasis mt-1">
                Este es tu panel de monitoreo. Aquí ves el estado de lo que TuBoleta vigila por ti.
            </p>
        </div>

        <v-row>
            <v-col
                cols="12"
                sm="6"
                md="3"
            >
                <v-card
                    rounded="lg"
                    class="pa-4 h-100"
                >
                    <div class="d-flex align-center justify-space-between">
                        <div>
                            <div class="text-caption text-medium-emphasis">Búsquedas activas</div>
                            <div class="text-h4 font-weight-bold mt-1">{{ activeSearches }}</div>
                            <div class="text-caption text-medium-emphasis">
                                de {{ searches.length }} en total
                            </div>
                        </div>
                        <v-avatar
                            color="primary"
                            variant="tonal"
                            size="48"
                        >
                            <v-icon icon="mdi-magnify" />
                        </v-avatar>
                    </div>
                </v-card>
            </v-col>

            <v-col
                cols="12"
                sm="6"
                md="3"
            >
                <v-card
                    rounded="lg"
                    class="pa-4 h-100"
                >
                    <div class="d-flex align-center justify-space-between">
                        <div>
                            <div class="text-caption text-medium-emphasis">Eventos monitoreados</div>
                            <div class="text-h4 font-weight-bold mt-1">{{ totalEvents }}</div>
                            <div class="text-caption text-medium-emphasis">en todas tus búsquedas</div>
                        </div>
                        <v-avatar
                            color="info"
                            variant="tonal"
                            size="48"
                        >
                            <v-icon icon="mdi-calendar-star" />
                        </v-avatar>
                    </div>
                </v-card>
            </v-col>

            <v-col
                cols="12"
                sm="6"
                md="3"
            >
                <v-card
                    rounded="lg"
                    class="pa-4 h-100"
                    :to="{ name: 'notifications' }"
                >
                    <div class="d-flex align-center justify-space-between">
                        <div>
                            <div class="text-caption text-medium-emphasis">Notificaciones sin leer</div>
                            <div class="text-h4 font-weight-bold mt-1">{{ unreadCount }}</div>
                            <div class="text-caption text-medium-emphasis">ir al centro de avisos</div>
                        </div>
                        <v-avatar
                            :color="unreadCount > 0 ? 'warning' : 'success'"
                            variant="tonal"
                            size="48"
                        >
                            <v-icon icon="mdi-bell" />
                        </v-avatar>
                    </div>
                </v-card>
            </v-col>

            <v-col
                cols="12"
                sm="6"
                md="3"
            >
                <v-card
                    rounded="lg"
                    class="pa-4 h-100"
                    :to="{ name: 'destinations' }"
                >
                    <div class="d-flex align-center justify-space-between">
                        <div>
                            <div class="text-caption text-medium-emphasis">Destinos activos</div>
                            <div class="text-h4 font-weight-bold mt-1">{{ activeDestinations }}</div>
                            <div class="text-caption text-medium-emphasis">correos donde te avisamos</div>
                        </div>
                        <v-avatar
                            color="primary"
                            variant="tonal"
                            size="48"
                        >
                            <v-icon icon="mdi-email" />
                        </v-avatar>
                    </div>
                </v-card>
            </v-col>
        </v-row>

        <v-row class="mt-2">
            <v-col cols="12">
                <v-card rounded="lg">
                    <v-card-title class="d-flex align-center justify-space-between">
                        <span class="text-subtitle-1 font-weight-bold">Tus búsquedas</span>
                        <v-btn
                            variant="text"
                            color="primary"
                            size="small"
                            :to="{ name: 'searches' }"
                        >
                            Ver todas
                        </v-btn>
                    </v-card-title>
                    <v-divider />

                    <v-list
                        v-if="searches.length > 0"
                        lines="two"
                    >
                        <template
                            v-for="(s, i) in searches.slice(0, 5)"
                            :key="s.id"
                        >
                            <v-list-item :to="{ name: 'search-events', params: { id: s.id } }">
                                <template #prepend>
                                    <v-avatar
                                        :color="s.status === 'ACTIVE' ? 'primary' : 'grey'"
                                        variant="tonal"
                                        size="40"
                                    >
                                        <v-icon icon="mdi-radar" />
                                    </v-avatar>
                                </template>
                                <v-list-item-title class="font-weight-medium">
                                    {{ s.term }}
                                </v-list-item-title>
                                <v-list-item-subtitle>
                                    {{ formatFrequency(s.checkFrequencyMinutes) }} ·
                                    {{ s.eventsCount }} eventos ·
                                    {{ s.providers.length }} fuentes
                                </v-list-item-subtitle>
                                <template #append>
                                    <v-chip
                                        :color="s.status === 'ACTIVE' ? 'success' : 'warning'"
                                        size="small"
                                        variant="tonal"
                                    >
                                        {{ s.status === 'ACTIVE' ? 'Activa' : 'Pausada' }}
                                    </v-chip>
                                </template>
                            </v-list-item>
                            <v-divider v-if="i < Math.min(searches.length, 5) - 1" />
                        </template>
                    </v-list>

                    <div
                        v-else
                        class="pa-8 text-center"
                    >
                        <v-icon
                            icon="mdi-magnify-scan"
                            size="48"
                            class="text-medium-emphasis mb-3"
                        />
                        <p class="text-medium-emphasis mb-4">
                            Todavía no tienes búsquedas. Crea la primera para empezar a monitorear.
                        </p>
                        <v-btn
                            color="primary"
                            prepend-icon="mdi-plus"
                            :to="{ name: 'searches' }"
                        >
                            Nueva búsqueda
                        </v-btn>
                    </div>
                </v-card>
            </v-col>
        </v-row>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useSearches } from '@/composables/searches/useSearches'
import { formatFrequency } from '@/utils/text/formatFrequency'
import { useNotifications } from '@/composables/notifications/useNotifications'
import { useDestinations } from '@/composables/destinations/useDestinations'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
const { searches, getSearches } = useSearches()
const { unreadCount, getUnreadCount } = useNotifications()
const { destinations, getDestinations } = useDestinations()

const userName = computed(() => authStore.user?.name || 'usuario')
const activeSearches = computed(() => searches.value.filter((s) => s.status === 'ACTIVE').length)
const totalEvents = computed(() => searches.value.reduce((acc, s) => acc + (s.eventsCount || 0), 0))
const activeDestinations = computed(() => destinations.value.filter((d) => d.isActive).length)

onMounted(() => {
    getSearches()
    getUnreadCount()
    getDestinations()
})
</script>
