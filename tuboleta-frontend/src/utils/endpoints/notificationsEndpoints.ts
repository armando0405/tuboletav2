// NotificationController: /api/notifications/** (inbox in-app, REQ-NOT-003)
const CONTROLLER_NOTIFICATIONS = '/notifications'

const GET_NOTIFICATIONS = CONTROLLER_NOTIFICATIONS
const GET_UNREAD_COUNT = `${CONTROLLER_NOTIFICATIONS}/unread-count`
const POST_MARK_READ = (id: number): string => `${CONTROLLER_NOTIFICATIONS}/${id}/read`
const POST_MARK_ALL_READ = `${CONTROLLER_NOTIFICATIONS}/read-all`

export { GET_NOTIFICATIONS, GET_UNREAD_COUNT, POST_MARK_READ, POST_MARK_ALL_READ }
