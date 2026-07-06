import { ref } from 'vue'
import { notificationsService } from '@/utils/services/notificationsServices'
import { useNotify } from '@/composables/useNotify'
import type { Notification } from '@/types/services/Notification'

// Estado compartido a nivel de módulo (patrón de patron-frontend.md): el
// badge de no leídas (menú lateral, Main.vue) y el centro de notificaciones
// (NotificationsInbox) leen y escriben el mismo `unreadCount`/`notifications`
// sin duplicar llamadas ni desincronizarse entre sí.
const loading = ref<boolean>(true)
const notifications = ref<Notification[]>([])
const unreadOnly = ref<boolean>(false)
const unreadCount = ref<number>(0)
const markingAllRead = ref<boolean>(false)

export const useNotifications = () => {
    const { notify } = useNotify()

    const getUnreadCount = async (): Promise<void> => {
        try {
            const { data } = await notificationsService.getUnreadCount()
            unreadCount.value = data?.object ?? 0
        } catch (err) {
            console.error('Error al obtener el contador de no leídas', err)
        }
    }

    const getNotifications = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await notificationsService.getNotifications(unreadOnly.value)
            notifications.value = data?.list || []
        } catch (err) {
            console.error('Error al obtener las notificaciones', err)
        } finally {
            loading.value = false
        }
    }

    // Toggle "solo no leídas": recarga la lista con el filtro nuevo. NUNCA
    // marca nada como leído por sí solo (REQ-NOT-003) — es puramente un
    // filtro de lectura.
    const setUnreadOnly = (value: boolean): void => {
        unreadOnly.value = value
        void getNotifications()
    }

    // Marcar leída es SIEMPRE una acción explícita del usuario (botón por
    // ítem o "marcar todas") — nunca se dispara por visitar/listar (REQ-NOT-003).
    const markRead = async (item: Notification): Promise<void> => {
        if (item.readAt) return
        try {
            await notificationsService.postMarkRead(item.id)
            const now = new Date().toISOString()
            if (unreadOnly.value) {
                notifications.value = notifications.value.filter((n) => n.id !== item.id)
            } else {
                notifications.value = notifications.value.map((n) =>
                    n.id === item.id ? { ...n, readAt: now } : n,
                )
            }
            if (unreadCount.value > 0) unreadCount.value -= 1
        } catch (err) {
            console.error('Error al marcar la notificación como leída', err)
        }
    }

    const markAllRead = async (): Promise<void> => {
        try {
            markingAllRead.value = true
            await notificationsService.postMarkAllRead()
            const now = new Date().toISOString()
            notifications.value = unreadOnly.value
                ? []
                : notifications.value.map((n) => ({ ...n, readAt: n.readAt ?? now }))
            unreadCount.value = 0
            notify('Todas las notificaciones se marcaron como leídas', 'success')
        } catch (err) {
            console.error('Error al marcar todas las notificaciones como leídas', err)
        } finally {
            markingAllRead.value = false
        }
    }

    return {
        loading,
        notifications,
        unreadOnly,
        unreadCount,
        markingAllRead,
        getNotifications,
        getUnreadCount,
        setUnreadOnly,
        markRead,
        markAllRead,
    }
}
