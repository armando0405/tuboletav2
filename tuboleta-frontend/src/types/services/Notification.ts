// Refleja NotificationResponse (NotificationController / inbox, REQ-NOT-003).
// readAt === null es "no leída"; eventTitle es null para PROVIDER_DISABLED.
export type NotificationType = 'NEW' | 'CHANGED' | 'REMOVED' | 'PROVIDER_DISABLED'

export type Notification = {
    id: number
    type: NotificationType
    searchTerm: string
    eventTitle: string | null
    createdAt: string
    readAt: string | null
}
