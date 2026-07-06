import api from '@/plugins/axios'
import { NOTIFICATIONS } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type { Notification } from '@/types/services/Notification'
import type { AxiosResponse } from 'axios'

const getNotifications = (
    unreadOnly = false,
): Promise<AxiosResponse<ObjectListResponse<Notification>>> =>
    api.get<ObjectListResponse<Notification>>(NOTIFICATIONS.GET_NOTIFICATIONS, {
        params: { unreadOnly },
    })

const getUnreadCount = (): Promise<AxiosResponse<ObjectResponse<number>>> =>
    api.get<ObjectResponse<number>>(NOTIFICATIONS.GET_UNREAD_COUNT)

const postMarkRead = (id: number): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(NOTIFICATIONS.POST_MARK_READ(id))

const postMarkAllRead = (): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(NOTIFICATIONS.POST_MARK_ALL_READ)

export const notificationsService = {
    getNotifications,
    getUnreadCount,
    postMarkRead,
    postMarkAllRead,
}
