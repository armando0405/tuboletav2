import { ref, computed } from 'vue'
import { searchesService } from '@/utils/services/searchesServices'
import { notificationsService } from '@/utils/services/notificationsServices'
import { useSearches } from '@/composables/searches/useSearches'
import type { Event } from '@/types/services/Event'
import type { Search } from '@/types/services/Search'
import type { NotificationType } from '@/types/services/Notification'

const loading = ref<boolean>(true)
const events = ref<Event[]>([])
const search = ref<Search | null>(null)

// Destaque de "nuevo/cambiado/eliminado" (REQ-FE-003): se apoya en el mismo
// modelo notifications + read_at (REQ-NOT-003), no se inventa un mecanismo
// aparte. NotificationResponse no trae eventId/searchId (solo searchTerm y
// eventTitle como texto), así que el cruce es un best-effort por
// (searchTerm === término de esta búsqueda) + (eventTitle === título del
// evento) entre las notificaciones no leídas. Colisiones de título dentro de
// la misma búsqueda (p.ej. mismo evento en dos proveedores) son un caso
// límite aceptado dado lo que expone hoy el backend.
const highlightByTitle = ref<Map<string, NotificationType>>(new Map())

export const useSearchEvents = () => {
    const { searches, getSearches } = useSearches()

    const resolveSearch = async (searchId: number): Promise<void> => {
        if (searches.value.length === 0) {
            await getSearches()
        }
        search.value = searches.value.find((s) => s.id === searchId) || null
    }

    const loadHighlights = async (term: string): Promise<void> => {
        try {
            const { data } = await notificationsService.getNotifications(true)
            const map = new Map<string, NotificationType>()
            for (const n of data?.list || []) {
                if (n.searchTerm === term && n.eventTitle) {
                    map.set(n.eventTitle, n.type)
                }
            }
            highlightByTitle.value = map
        } catch (err) {
            console.error('Error al obtener el destaque de novedades', err)
            highlightByTitle.value = new Map()
        }
    }

    const getEvents = async (searchId: number): Promise<void> => {
        try {
            loading.value = true
            await resolveSearch(searchId)
            const [{ data }] = await Promise.all([
                searchesService.getSearchEvents(searchId),
                search.value ? loadHighlights(search.value.term) : Promise.resolve(),
            ])
            events.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener los eventos de la búsqueda', err)
        } finally {
            loading.value = false
        }
    }

    const highlightFor = (event: Event): NotificationType | null =>
        highlightByTitle.value.get(event.title) || null

    // El link "https://www.tuboleta.com{externalId}" solo aplica cuando el
    // externalId es la ruta relativa que devuelve el scraper de TuBoleta
    // (empieza con "/"); otros proveedores futuros podrían usar otro formato
    // de id, en cuyo caso no se arma un link (REQ-FE-003: "cuando aplique").
    const sourceUrl = (event: Event): string | null => {
        if (!event.externalId?.startsWith('/')) return null
        return `https://www.tuboleta.com${event.externalId}`
    }

    const hasHighlights = computed<boolean>(() => highlightByTitle.value.size > 0)

    // Limpia el estado module-level (logout): evita que los eventos/destaque
    // de una búsqueda queden visibles para el siguiente usuario de la misma
    // pestaña.
    const resetAll = (): void => {
        loading.value = true
        events.value = []
        search.value = null
        highlightByTitle.value = new Map()
    }

    return {
        loading,
        events,
        search,
        hasHighlights,
        getEvents,
        highlightFor,
        sourceUrl,
        resetAll,
    }
}
