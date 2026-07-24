import { ref } from 'vue'
import { isAxiosError } from 'axios'
import { searchesService } from '@/utils/services/searchesServices'
import { providersService } from '@/utils/services/providersServices'
import { destinationsService } from '@/utils/services/destinationsServices'
import { frequenciesService } from '@/utils/services/frequenciesServices'
import { notificationsService } from '@/utils/services/notificationsServices'
import { useNotify } from '@/composables/useNotify'
import type { Search, SearchCreateRequest, SearchUpdateRequest } from '@/types/services/Search'
import type { Provider } from '@/types/services/Provider'
import type { Destination } from '@/types/services/Destination'
import type { Frequency } from '@/types/services/Frequency'
import type { ObjectResponse } from '@/types/services/Responses'

// Estado compartido a nivel de módulo (patrón de patron-frontend.md): la
// vista de listado, el header (botón + diálogo de alta) y las cards
// consumen el mismo estado sin prop-drilling.
const loading = ref<boolean>(true)
const searches = ref<Search[]>([])

const catalogsLoading = ref<boolean>(false)
const providerCatalog = ref<Provider[]>([])
const destinationCatalog = ref<Destination[]>([])
const frequencyCatalog = ref<Frequency[]>([])

const showFormDialog = ref<boolean>(false)
const formMode = ref<'create' | 'edit'>('create')
const editingSearch = ref<Search | null>(null)
const submitting = ref<boolean>(false)
// Id de la búsqueda cuya corrida manual ("ejecutar ahora") está en curso, para
// el spinner por card. null = ninguna corriendo.
const runningId = ref<number | null>(null)
// Mensaje inline (además del toast global del interceptor de axios) para el
// 409 de término duplicado — se muestra bajo el campo término sin romper el
// formulario (REQ-FE-001).
const formError = ref<string | null>(null)

// Badge de "novedades" por card (diseno-frontend.md#Dashboard): cuántas
// notificaciones no leídas hay por cada búsqueda. NotificationResponse no
// trae searchId, solo searchTerm (texto), así que el cruce es un
// best-effort por término exacto — el mismo patrón ya usado en
// useSearchEvents.ts. No hay colisión posible entre búsquedas propias porque
// term_normalized es único por usuario (excluyendo DELETED), así que dos
// búsquedas activas/inactivas del mismo usuario nunca comparten term.
const unreadCountByTerm = ref<Map<string, number>>(new Map())

export const useSearches = () => {
    const { notify } = useNotify()

    const getSearches = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await searchesService.getSearches()
            searches.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener las búsquedas', err)
        } finally {
            loading.value = false
        }
    }

    // Carga (una vez por listado) las notificaciones no leídas y las agrupa
    // por término para derivar el badge de novedades de cada card. Falla en
    // silencio: sin badge no se rompe el listado de búsquedas.
    const loadUnreadBadges = async (): Promise<void> => {
        try {
            const { data } = await notificationsService.getNotifications(true)
            const map = new Map<string, number>()
            for (const n of data?.list || []) {
                map.set(n.searchTerm, (map.get(n.searchTerm) || 0) + 1)
            }
            unreadCountByTerm.value = map
        } catch (err) {
            console.error('Error al obtener el badge de novedades', err)
            unreadCountByTerm.value = new Map()
        }
    }

    const unreadCountFor = (search: Search): number => unreadCountByTerm.value.get(search.term) || 0

    // Proveedores ACTIVE + destinos propios para los selects del formulario.
    // Se recarga cada vez que se abre el diálogo (dataset pequeño, evita
    // mostrar destinos desactualizados si el usuario acaba de crear uno).
    const loadCatalogs = async (): Promise<void> => {
        try {
            catalogsLoading.value = true
            const [providersRes, destinationsRes, frequenciesRes] = await Promise.all([
                providersService.getProviders(),
                destinationsService.getDestinations(),
                frequenciesService.getFrequencies(),
            ])
            providerCatalog.value = providersRes.data?.list || []
            destinationCatalog.value = destinationsRes.data?.list || []
            frequencyCatalog.value = frequenciesRes.data?.list || []
        } catch (err) {
            console.error('Error al cargar proveedores/destinos', err)
        } finally {
            catalogsLoading.value = false
        }
    }

    const openCreateDialog = (): void => {
        formMode.value = 'create'
        editingSearch.value = null
        formError.value = null
        showFormDialog.value = true
        void loadCatalogs()
    }

    const openEditDialog = (search: Search): void => {
        formMode.value = 'edit'
        editingSearch.value = search
        formError.value = null
        showFormDialog.value = true
        void loadCatalogs()
    }

    const closeFormDialog = (): void => {
        showFormDialog.value = false
        editingSearch.value = null
        formError.value = null
    }

    const createSearch = async (payload: SearchCreateRequest): Promise<boolean> => {
        formError.value = null
        try {
            submitting.value = true
            const { data } = await searchesService.postSearch(payload)
            const created = data.object
            if (created) searches.value = [created, ...searches.value]
            notify(`Búsqueda "${payload.term}" creada`, 'success')
            closeFormDialog()
            return true
        } catch (err) {
            if (isAxiosError<ObjectResponse>(err) && err.response?.status === 409) {
                // El toast global (interceptor de axios) ya avisó; aquí además
                // dejamos el error pegado al campo término.
                formError.value =
                    err.response?.data?.msg || 'Ya existe una búsqueda con un término equivalente.'
            } else {
                console.error('Error al crear la búsqueda', err)
            }
            return false
        } finally {
            submitting.value = false
        }
    }

    const editSearch = async (id: number, payload: SearchUpdateRequest): Promise<boolean> => {
        formError.value = null
        try {
            submitting.value = true
            const { data } = await searchesService.patchSearch(id, payload)
            const updated = data.object
            if (updated) {
                searches.value = searches.value.map((s) => (s.id === id ? updated : s))
            }
            notify('Búsqueda actualizada', 'success')
            closeFormDialog()
            return true
        } catch (err) {
            console.error('Error al editar la búsqueda', err)
            return false
        } finally {
            submitting.value = false
        }
    }

    const togglePair = async (search: Search, providerId: number): Promise<void> => {
        try {
            const { data } = await searchesService.patchToggleSearchProvider(search.id, providerId)
            const updated = data.object
            if (updated) {
                searches.value = searches.value.map((s) => (s.id === search.id ? updated : s))
            }
            notify('Estado del proveedor actualizado', 'success')
        } catch (err) {
            console.error('Error al pausar/reanudar el proveedor', err)
        }
    }

    // Pausa/reanuda la búsqueda COMPLETA (todos sus proveedores a la vez,
    // REQ-FE-001). Sin confirmación: es instantáneamente reversible con el
    // mismo control, igual que togglePair (par por proveedor) — mantener el
    // mismo nivel de fricción entre ambos toggles.
    const toggleStatus = async (search: Search): Promise<void> => {
        try {
            const { data } = await searchesService.patchToggleSearchStatus(search.id)
            const updated = data.object
            if (updated) {
                searches.value = searches.value.map((s) => (s.id === search.id ? updated : s))
            }
            notify(
                updated?.status === 'INACTIVE' ? 'Búsqueda pausada' : 'Búsqueda reanudada',
                'success',
            )
        } catch (err) {
            console.error('Error al pausar/reanudar la búsqueda', err)
        }
    }

    // Dispara la corrida de monitoreo AHORA (sin esperar el horario, REQ nuevo):
    // corre el scraping/detección/notificación en el momento y refresca el
    // listado (contador de eventos) y el badge de novedades. Da feedback con el
    // total de eventos encontrados para validar que el servicio funciona.
    const runNow = async (search: Search): Promise<void> => {
        try {
            runningId.value = search.id
            const { data } = await searchesService.postRunSearchNow(search.id)
            const total = data.object ?? 0
            notify(`Corrida ejecutada para "${search.term}": ${total} evento(s) en total`, 'success')
            await Promise.all([getSearches(), loadUnreadBadges()])
        } catch (err) {
            console.error('Error al ejecutar la corrida manual', err)
        } finally {
            runningId.value = null
        }
    }

    const deleteSearch = async (search: Search): Promise<void> => {
        const result = await window.swal.fire({
            title: `¿Eliminar "${search.term}"?`,
            text: 'Se dejará de monitorear en todos sus proveedores. El histórico de eventos y notificaciones se conserva.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Eliminar',
        })
        if (!result.isConfirmed) return

        try {
            await searchesService.deleteSearch(search.id)
            searches.value = searches.value.filter((s) => s.id !== search.id)
            notify('Búsqueda eliminada', 'success')
        } catch (err) {
            console.error('Error al eliminar la búsqueda', err)
        }
    }

    // Limpia todo el estado module-level (logout, REQ-SEG): sin esto, el
    // listado/formulario del usuario saliente quedaría visible un instante
    // para el siguiente que inicie sesión en la misma pestaña.
    const resetAll = (): void => {
        loading.value = true
        searches.value = []
        unreadCountByTerm.value = new Map()
        catalogsLoading.value = false
        providerCatalog.value = []
        destinationCatalog.value = []
        frequencyCatalog.value = []
        showFormDialog.value = false
        formMode.value = 'create'
        editingSearch.value = null
        submitting.value = false
        formError.value = null
        runningId.value = null
    }

    return {
        loading,
        searches,
        catalogsLoading,
        providerCatalog,
        destinationCatalog,
        frequencyCatalog,
        showFormDialog,
        formMode,
        editingSearch,
        submitting,
        formError,
        runningId,
        getSearches,
        loadUnreadBadges,
        unreadCountFor,
        openCreateDialog,
        openEditDialog,
        closeFormDialog,
        createSearch,
        editSearch,
        togglePair,
        toggleStatus,
        runNow,
        deleteSearch,
        resetAll,
    }
}
