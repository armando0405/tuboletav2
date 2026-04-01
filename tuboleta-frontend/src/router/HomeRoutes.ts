import type { RouteRecordRaw } from 'vue-router'

const HomeRoutes: RouteRecordRaw[] = [
    {
        name: 'Dashboard',
        path: '/',
        component: () => import('@/views/dashboard/Index.vue'),
    },
]

export default HomeRoutes
