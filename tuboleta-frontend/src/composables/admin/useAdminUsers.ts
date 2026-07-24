import { ref } from 'vue'
import { adminUsersService } from '@/utils/services/adminUsersServices'
import { useNotify } from '@/composables/useNotify'
import type { AdminUser } from '@/types/services/AdminUser'
import type { UserRole } from '@/types/services/User'

// Estado compartido a nivel de módulo (patrón patron-frontend.md), solo ADMIN:
// listado de usuarios + acciones de estado/rol.
const loading = ref<boolean>(true)
const users = ref<AdminUser[]>([])
const submitting = ref<boolean>(false)

export const useAdminUsers = () => {
    const { notify } = useNotify()

    const getUsers = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await adminUsersService.getUsers()
            users.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener los usuarios', err)
        } finally {
            loading.value = false
        }
    }

    const setStatus = async (user: AdminUser, active: boolean): Promise<void> => {
        const result = await window.swal.fire({
            title: active ? `¿Activar a "${user.name}"?` : `¿Desactivar a "${user.name}"?`,
            text: active
                ? 'Podrá volver a iniciar sesión y sus búsquedas se reanudan.'
                : 'No podrá iniciar sesión y sus búsquedas quedan en pausa.',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: active ? 'Activar' : 'Desactivar',
        })
        if (!result.isConfirmed) return

        try {
            submitting.value = true
            const { data } = await adminUsersService.patchUserStatus(user.id, active)
            applyUpdated(data.object)
            notify(`Usuario "${user.name}" ${active ? 'activado' : 'desactivado'}`, 'success')
        } catch (err) {
            console.error('Error al cambiar el estado del usuario', err)
        } finally {
            submitting.value = false
        }
    }

    const setRole = async (user: AdminUser, role: UserRole): Promise<void> => {
        try {
            submitting.value = true
            const { data } = await adminUsersService.patchUserRole(user.id, role)
            applyUpdated(data.object)
            notify(`Rol de "${user.name}" actualizado a ${role}`, 'success')
        } catch (err) {
            console.error('Error al cambiar el rol del usuario', err)
        } finally {
            submitting.value = false
        }
    }

    const applyUpdated = (updated: AdminUser | null | undefined): void => {
        if (updated) {
            users.value = users.value.map((u) => (u.id === updated.id ? updated : u))
        }
    }

    const resetAll = (): void => {
        loading.value = true
        users.value = []
        submitting.value = false
    }

    return { loading, users, submitting, getUsers, setStatus, setRole, resetAll }
}
