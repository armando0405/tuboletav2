import { ref } from 'vue'
import { isAxiosError } from 'axios'
import { frequenciesService } from '@/utils/services/frequenciesServices'
import { useNotify } from '@/composables/useNotify'
import type { Frequency, FrequencyRequest } from '@/types/services/Frequency'
import type { ObjectResponse } from '@/types/services/Responses'

// Estado compartido a nivel de módulo (patron-frontend.md), solo ADMIN: gestión
// del catálogo de frecuencias (REQ-BUS-005). Borrar una frecuencia no afecta a
// las búsquedas que ya la usan (guardan su propio valor en minutos).
const loading = ref<boolean>(true)
const frequencies = ref<Frequency[]>([])
const showFormDialog = ref<boolean>(false)
const editing = ref<Frequency | null>(null)
const submitting = ref<boolean>(false)
const formError = ref<string | null>(null)

const sortByMinutes = (list: Frequency[]): Frequency[] => [...list].sort((a, b) => a.minutes - b.minutes)

export const useFrequenciesAdmin = () => {
    const { notify } = useNotify()

    const getFrequencies = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await frequenciesService.getAdminFrequencies()
            frequencies.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener las frecuencias', err)
        } finally {
            loading.value = false
        }
    }

    const openCreate = (): void => {
        editing.value = null
        formError.value = null
        showFormDialog.value = true
    }

    const openEdit = (frequency: Frequency): void => {
        editing.value = frequency
        formError.value = null
        showFormDialog.value = true
    }

    const closeDialog = (): void => {
        showFormDialog.value = false
        editing.value = null
        formError.value = null
    }

    const save = async (payload: FrequencyRequest): Promise<boolean> => {
        formError.value = null
        try {
            submitting.value = true
            if (editing.value) {
                const { data } = await frequenciesService.putFrequency(editing.value.id, payload)
                const updated = data.object
                if (updated) {
                    frequencies.value = sortByMinutes(
                        frequencies.value.map((f) => (f.id === updated.id ? updated : f)),
                    )
                }
                notify('Frecuencia actualizada', 'success')
            } else {
                const { data } = await frequenciesService.postFrequency(payload)
                const created = data.object
                if (created) frequencies.value = sortByMinutes([...frequencies.value, created])
                notify('Frecuencia creada', 'success')
            }
            closeDialog()
            return true
        } catch (err) {
            if (isAxiosError<ObjectResponse>(err) && err.response?.status === 409) {
                formError.value =
                    err.response?.data?.msg || 'Ya existe una frecuencia con ese valor en minutos.'
            } else {
                console.error('Error al guardar la frecuencia', err)
            }
            return false
        } finally {
            submitting.value = false
        }
    }

    const remove = async (frequency: Frequency): Promise<void> => {
        const result = await window.swal.fire({
            title: `¿Eliminar "${frequency.label}"?`,
            text: 'Las búsquedas que ya la usan conservan su intervalo; solo deja de ofrecerse para nuevas búsquedas.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Eliminar',
        })
        if (!result.isConfirmed) return

        try {
            await frequenciesService.deleteFrequency(frequency.id)
            frequencies.value = frequencies.value.filter((f) => f.id !== frequency.id)
            notify('Frecuencia eliminada', 'success')
        } catch (err) {
            console.error('Error al eliminar la frecuencia', err)
        }
    }

    const resetAll = (): void => {
        loading.value = true
        frequencies.value = []
        showFormDialog.value = false
        editing.value = null
        submitting.value = false
        formError.value = null
    }

    return {
        loading,
        frequencies,
        showFormDialog,
        editing,
        submitting,
        formError,
        getFrequencies,
        openCreate,
        openEdit,
        closeDialog,
        save,
        remove,
        resetAll,
    }
}
