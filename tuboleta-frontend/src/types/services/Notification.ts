// Refleja NotificationResponse (NotificationController / inbox, REQ-NOT-003).
// readAt === null es "no leída"; eventTitle es null para PROVIDER_DISABLED.
export type NotificationType = 'NEW' | 'CHANGED' | 'REMOVED' | 'PROVIDER_DISABLED'

export type Notification = {
    id: number
    type: NotificationType
    searchId: number | null
    searchTerm: string
    eventId: number | null
    eventTitle: string | null
    createdAt: string
    readAt: string | null
}
