import { ref } from 'vue'
import { isAxiosError } from 'axios'
import { destinationsService } from '@/utils/services/destinationsServices'
import { useNotify } from '@/composables/useNotify'
import type { Destination, DestinationRequest } from '@/types/services/Destination'
import type { ObjectResponse } from '@/types/services/Responses'

// Estado compartido a nivel de módulo (patrón de patron-frontend.md): la
// vista de listado, el header (botón + diálogo de alta) y la tabla consumen
// el mismo estado sin prop-drilling.
const loading = ref<boolean>(true)
const destinations = ref<Destination[]>([])

const showFormDialog = ref<boolean>(false)
const submitting = ref<boolean>(false)
// Mensaje inline (además del toast global del interceptor de axios) para el
// 409 de destino duplicado o canal inactivo — se muestra bajo el campo email
// sin romper el formulario.
const formError = ref<string | null>(null)

export const useDestinations = () => {
    const { notify } = useNotify()

    const getDestinations = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await destinationsService.getDestinations()
            destinations.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener los destinos', err)
        } finally {
            loading.value = false
        }
    }

    const openCreateDialog = (): void => {
        formError.value = null
        showFormDialog.value = true
    }

    const closeFormDialog = (): void => {
        showFormDialog.value = false
        formError.value = null
    }

    const createDestination = async (payload: DestinationRequest): Promise<boolean> => {
        formError.value = null
        try {
            submitting.value = true
            const { data } = await destinationsService.postDestination(payload)
            const created = data.object
            if (created) destinations.value = [created, ...destinations.value]
            notify(`Destino "${payload.destination}" agregado`, 'success')
            closeFormDialog()
            return true
        } catch (err) {
            if (isAxiosError<ObjectResponse>(err) && err.response?.status === 409) {
                // El toast global (interceptor de axios) ya avisó del 409
                // (destino duplicado o canal EMAIL inactivo); aquí además
                // dejamos el detalle pegado al campo del formulario.
                formError.value =
                    err.response?.data?.msg ||
                    'No se pudo agregar el destino: ya existe o el canal no está activo.'
            } else {
                console.error('Error al agregar el destino', err)
            }
            return false
        } finally {
            submitting.value = false
        }
    }

    const toggleDestination = async (destination: Destination): Promise<void> => {
        try {
            const { data } = await destinationsService.patchToggleDestination(destination.id)
            const updated = data.object
            if (updated) {
                destinations.value = destinations.value.map((d) =>
                    d.id === destination.id ? updated : d,
                )
            }
            notify(updated?.isActive ? 'Destino activado' : 'Destino desactivado', 'success')
        } catch (err) {
            console.error('Error al cambiar el estado del destino', err)
        }
    }

    // Limpia el estado module-level (logout): evita fuga de destinos entre
    // sesiones en la misma pestaña.
    const resetAll = (): void => {
        loading.value = true
        destinations.value = []
        showFormDialog.value = false
        submitting.value = false
        formError.value = null
    }

    return {
        loading,
        destinations,
        showFormDialog,
        submitting,
        formError,
        getDestinations,
        openCreateDialog,
        closeFormDialog,
        createDestination,
        toggleDestination,
        resetAll,
    }
}
