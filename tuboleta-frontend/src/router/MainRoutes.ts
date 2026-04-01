import HomeRoutes from './HomeRoutes'
import AdminRoutes from './AdminRoute'


const MainRoutes = {
    path: '/',
    meta: {
        requiresAuth: true,
    },
    redirect: '/home',
    component: () => import('@/layouts/full/FullLayout.vue'),
    children: [
        {
            name: 'home',
            path: '/home',
            component: () => import('@/views/dashboard/Index.vue'),
        },
        ...HomeRoutes,
        ...AdminRoutes,
    ],
}

export default MainRoutes
