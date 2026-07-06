import { ref } from 'vue'
import { isAxiosError } from 'axios'
import { searchesService } from '@/utils/services/searchesServices'
import { providersService } from '@/utils/services/providersServices'
import { destinationsService } from '@/utils/services/destinationsServices'
import { useNotify } from '@/composables/useNotify'
import type { Search, SearchCreateRequest, SearchUpdateRequest } from '@/types/services/Search'
import type { Provider } from '@/types/services/Provider'
import type { Destination } from '@/types/services/Destination'
import type { ObjectResponse } from '@/types/services/Responses'

// Estado compartido a nivel de módulo (patrón de patron-frontend.md): la
// vista de listado, el header (botón + diálogo de alta) y las cards
// consumen el mismo estado sin prop-drilling.
const loading = ref<boolean>(true)
const searches = ref<Search[]>([])

const catalogsLoading = ref<boolean>(false)
const providerCatalog = ref<Provider[]>([])
const destinationCatalog = ref<Destination[]>([])

const showFormDialog = ref<boolean>(false)
const formMode = ref<'create' | 'edit'>('create')
const editingSearch = ref<Search | null>(null)
const submitting = ref<boolean>(false)
// Mensaje inline (además del toast global del interceptor de axios) para el
// 409 de término duplicado — se muestra bajo el campo término sin romper el
// formulario (REQ-FE-001).
const formError = ref<string | null>(null)

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

    // Proveedores ACTIVE + destinos propios para los selects del formulario.
    // Se recarga cada vez que se abre el diálogo (dataset pequeño, evita
    // mostrar destinos desactualizados si el usuario acaba de crear uno).
    const loadCatalogs = async (): Promise<void> => {
        try {
            catalogsLoading.value = true
            const [providersRes, destinationsRes] = await Promise.all([
                providersService.getProviders(),
                destinationsService.getDestinations(),
            ])
            providerCatalog.value = providersRes.data?.list || []
            destinationCatalog.value = destinationsRes.data?.list || []
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

    return {
        loading,
        searches,
        catalogsLoading,
        providerCatalog,
        destinationCatalog,
        showFormDialog,
        formMode,
        editingSearch,
        submitting,
        formError,
        getSearches,
        openCreateDialog,
        openEditDialog,
        closeFormDialog,
        createSearch,
        editSearch,
        togglePair,
        deleteSearch,
    }
}
