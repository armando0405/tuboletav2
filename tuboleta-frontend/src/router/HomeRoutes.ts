import type { RouteRecordRaw } from 'vue-router'

// Rutas del panel autoservicio (REQ-FE-001..005). Búsquedas y eventos ya
// tienen pantalla real (T9b); notificaciones/destinos/admin siguen
// placeholder mínimo hasta T9c.
const HomeRoutes: RouteRecordRaw[] = [
    {
        name: 'Dashboard',
        path: '/',
        component: () => import('@/views/dashboard/Index.vue'),
    },
    {
        name: 'searches',
        path: '/busquedas',
        component: () => import('@/views/searches/SearchesList.vue'),
    },
    {
        name: 'search-events',
        path: '/busquedas/:id/eventos',
        component: () => import('@/views/searches/SearchEvents.vue'),
    },
    {
        name: 'notifications',
        path: '/notificaciones',
        component: () => import('@/views/notifications/NotificationsInbox.vue'),
    },
    {
        name: 'destinations',
        path: '/destinos',
        component: () => import('@/views/destinations/DestinationsList.vue'),
    },
    {
        name: 'admin-providers',
        path: '/admin/fuentes',
        meta: { requiresAdmin: true },
        component: () => import('@/views/admin/ProvidersAdmin.vue'),
    },
]

export default HomeRoutes
