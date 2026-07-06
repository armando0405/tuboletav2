import { ref } from 'vue'
import { adminProvidersService } from '@/utils/services/adminProvidersServices'
import { useNotify } from '@/composables/useNotify'
import type { ProviderAdmin } from '@/types/services/ProviderAdmin'

// Estado compartido a nivel de módulo (patrón de patron-frontend.md), solo
// ADMIN (REQ-FUE-002): la vista, el header y la tabla comparten el mismo
// listado y el diálogo de deshabilitación.
const loading = ref<boolean>(true)
const providers = ref<ProviderAdmin[]>([])

const showDisableDialog = ref<boolean>(false)
const disablingProvider = ref<ProviderAdmin | null>(null)
const submitting = ref<boolean>(false)

export const useProvidersAdmin = () => {
    const { notify } = useNotify()

    const getProviders = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await adminProvidersService.getProviders()
            providers.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener las fuentes', err)
        } finally {
            loading.value = false
        }
    }

    const openDisableDialog = (provider: ProviderAdmin): void => {
        disablingProvider.value = provider
        showDisableDialog.value = true
    }

    const closeDisableDialog = (): void => {
        showDisableDialog.value = false
        disablingProvider.value = null
    }

    // Razón obligatoria (REQ-FUE-002): el backend también la valida
    // (`@NotBlank`), pero el diálogo (ProviderDisableDialog) ya bloquea el
    // envío sin texto para dar feedback inmediato.
    const disableProvider = async (reason: string): Promise<boolean> => {
        if (!disablingProvider.value) return false
        try {
            submitting.value = true
            const { data } = await adminProvidersService.postDisableProvider(
                disablingProvider.value.id,
                {
                    reason,
                },
            )
            const updated = data.object
            if (updated) {
                providers.value = providers.value.map((p) => (p.id === updated.id ? updated : p))
            }
            notify(`Fuente "${disablingProvider.value.name}" deshabilitada`, 'success')
            closeDisableDialog()
            return true
        } catch (err) {
            console.error('Error al deshabilitar la fuente', err)
            return false
        } finally {
            submitting.value = false
        }
    }

    const enableProvider = async (provider: ProviderAdmin): Promise<void> => {
        const result = await window.swal.fire({
            title: `¿Habilitar "${provider.name}"?`,
            text: 'El scheduler retomará de inmediato las búsquedas que la usan.',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Habilitar',
        })
        if (!result.isConfirmed) return

        try {
            const { data } = await adminProvidersService.postEnableProvider(provider.id)
            const updated = data.object
            if (updated) {
                providers.value = providers.value.map((p) => (p.id === updated.id ? updated : p))
            }
            notify(`Fuente "${provider.name}" habilitada`, 'success')
        } catch (err) {
            console.error('Error al habilitar la fuente', err)
        }
    }

    return {
        loading,
        providers,
        showDisableDialog,
        disablingProvider,
        submitting,
        getProviders,
        openDisableDialog,
        closeDisableDialog,
        disableProvider,
        enableProvider,
    }
}
