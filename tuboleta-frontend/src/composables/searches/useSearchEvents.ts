import { ref, computed } from 'vue'
import { searchesService } from '@/utils/services/searchesServices'
import { notificationsService } from '@/utils/services/notificationsServices'
import { useSearches } from '@/composables/searches/useSearches'
import type { Event } from '@/types/services/Event'
import type { EventChange } from '@/types/services/EventChange'
import type { Search } from '@/types/services/Search'
import type { NotificationType } from '@/types/services/Notification'

const loading = ref<boolean>(true)
const events = ref<Event[]>([])
const search = ref<Search | null>(null)

// Historial de cambios de un evento (REQ-DET-002): diálogo bajo demanda.
const showHistory = ref<boolean>(false)
const historyEvent = ref<Event | null>(null)
const changes = ref<EventChange[]>([])
const historyLoading = ref<boolean>(false)

// Destaque de "nuevo/cambiado/eliminado" (REQ-FE-003): se apoya en el mismo
// modelo notifications + read_at (REQ-NOT-003). El cruce es robusto por
// IDENTIFICADOR: NotificationResponse ahora trae searchId/eventId, así que se
// mapea (eventId -> tipo de novedad) entre las notificaciones no leídas de esta
// búsqueda, sin ambigüedad por texto.
const highlightByEventId = ref<Map<number, NotificationType>>(new Map())

export const useSearchEvents = () => {
    const { searches, getSearches } = useSearches()

    const resolveSearch = async (searchId: number): Promise<void> => {
        if (searches.value.length === 0) {
            await getSearches()
        }
        search.value = searches.value.find((s) => s.id === searchId) || null
    }

    const loadHighlights = async (searchId: number): Promise<void> => {
        try {
            const { data } = await notificationsService.getNotifications(true)
            const map = new Map<number, NotificationType>()
            for (const n of data?.list || []) {
                if (n.searchId === searchId && n.eventId != null) {
                    map.set(n.eventId, n.type)
                }
            }
            highlightByEventId.value = map
        } catch (err) {
            console.error('Error al obtener el destaque de novedades', err)
            highlightByEventId.value = new Map()
        }
    }

    const getEvents = async (searchId: number): Promise<void> => {
        try {
            loading.value = true
            await resolveSearch(searchId)
            const [{ data }] = await Promise.all([
                searchesService.getSearchEvents(searchId),
                search.value ? loadHighlights(search.value.id) : Promise.resolve(),
            ])
            events.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener los eventos de la búsqueda', err)
        } finally {
            loading.value = false
        }
    }

    const highlightFor = (event: Event): NotificationType | null =>
        highlightByEventId.value.get(event.id) || null

    // El link "https://www.tuboleta.com{externalId}" solo aplica cuando el
    // externalId es la ruta relativa que devuelve el scraper de TuBoleta
    // (empieza con "/"); otros proveedores futuros podrían usar otro formato
    // de id, en cuyo caso no se arma un link (REQ-FE-003: "cuando aplique").
    const sourceUrl = (event: Event): string | null => {
        if (!event.externalId?.startsWith('/')) return null
        return `https://www.tuboleta.com${event.externalId}`
    }

    const hasHighlights = computed<boolean>(() => highlightByEventId.value.size > 0)

    // Abre el historial de cambios de un evento y lo carga desde el backend.
    const openHistory = async (event: Event): Promise<void> => {
        if (!search.value) return
        historyEvent.value = event
        changes.value = []
        showHistory.value = true
        try {
            historyLoading.value = true
            const { data } = await searchesService.getSearchEventChanges(search.value.id, event.id)
            changes.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener el historial de cambios', err)
        } finally {
            historyLoading.value = false
        }
    }

    const closeHistory = (): void => {
        showHistory.value = false
        historyEvent.value = null
        changes.value = []
    }

    // Limpia el estado module-level (logout): evita que los eventos/destaque
    // de una búsqueda queden visibles para el siguiente usuario de la misma
    // pestaña.
    const resetAll = (): void => {
        loading.value = true
        events.value = []
        search.value = null
        highlightByEventId.value = new Map()
        showHistory.value = false
        historyEvent.value = null
        changes.value = []
        historyLoading.value = false
    }

    return {
        loading,
        events,
        search,
        hasHighlights,
        showHistory,
        historyEvent,
        changes,
        historyLoading,
        getEvents,
        highlightFor,
        sourceUrl,
        openHistory,
        closeHistory,
        resetAll,
    }
}
