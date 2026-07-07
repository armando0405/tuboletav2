import type { RouteRecordRaw } from 'vue-router'

// Rutas del panel autoservicio (REQ-FE-001..005). Búsquedas/eventos (T9b) y
// notificaciones/destinos/admin de fuentes (T9c) ya tienen pantalla real.
// El path '/' ya lo sirve MainRoutes (redirect a '/home', name 'home') con
// el mismo Index.vue — no duplicar la entrada raíz aquí.
const HomeRoutes: RouteRecordRaw[] = [
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
