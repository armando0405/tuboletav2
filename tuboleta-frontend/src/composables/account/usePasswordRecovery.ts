import { ref } from 'vue'
import { authService } from '@/utils/services/authServices'
import { useNotify } from '@/composables/useNotify'

// Flujo público de recuperación de contraseña (sin sesión).
export const usePasswordRecovery = () => {
    const { notify } = useNotify()
    const submitting = ref<boolean>(false)

    // Siempre resuelve true salvo error de red: el backend no revela si el
    // correo existe (anti-enumeración), así que la vista muestra el mismo
    // mensaje pase lo que pase.
    const forgotPassword = async (email: string): Promise<boolean> => {
        try {
            submitting.value = true
            await authService.postForgotPassword({ email })
            return true
        } catch (err) {
            console.error('Error al solicitar la recuperación', err)
            return false
        } finally {
            submitting.value = false
        }
    }

    const resetPassword = async (token: string, newPassword: string): Promise<boolean> => {
        try {
            submitting.value = true
            await authService.postResetPassword({ token, newPassword })
            notify('Contraseña restablecida. Ya puedes iniciar sesión.', 'success')
            return true
        } catch (err) {
            console.error('Error al restablecer la contraseña', err)
            return false
        } finally {
            submitting.value = false
        }
    }

    return { submitting, forgotPassword, resetPassword }
}
