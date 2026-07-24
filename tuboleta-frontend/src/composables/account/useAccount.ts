import { ref } from 'vue'
import { authService } from '@/utils/services/authServices'
import { useAuthStore } from '@/stores/auth.store'
import { useNotify } from '@/composables/useNotify'
import type { ChangePasswordRequest, UpdateProfileRequest } from '@/types/services/Auth'

// Autoservicio de la propia cuenta: editar perfil y cambiar contraseña.
export const useAccount = () => {
    const { notify } = useNotify()
    const submitting = ref<boolean>(false)

    const changePassword = async (payload: ChangePasswordRequest): Promise<boolean> => {
        try {
            submitting.value = true
            await authService.patchPassword(payload)
            notify('Contraseña actualizada', 'success')
            return true
        } catch (err) {
            console.error('Error al cambiar la contraseña', err)
            return false
        } finally {
            submitting.value = false
        }
    }

    const updateProfile = async (payload: UpdateProfileRequest): Promise<boolean> => {
        try {
            submitting.value = true
            const { data } = await authService.patchProfile(payload)
            if (data.object) useAuthStore().setUser(data.object)
            notify('Perfil actualizado', 'success')
            return true
        } catch (err) {
            console.error('Error al actualizar el perfil', err)
            return false
        } finally {
            submitting.value = false
        }
    }

    return { submitting, changePassword, updateProfile }
}
