import { createRouter, createWebHistory } from 'vue-router'
import MainRoutes from './MainRoutes'
import AuthRoutes from './AuthRoutes'
import { useAuthStore } from '@/stores/auth.store'

export const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        MainRoutes,
        AuthRoutes,
        {
            path: '/:pathMatch(.*)*',
            component: () => import('@/views/pages/Error404.vue'),
        },
    ],
})

router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)

    // if (!requiresAuth) {
    //     if (to.name === 'login') {
    //         const isAuthenticated = await authStore.getUserLogged()
    //         if (isAuthenticated) return { name: 'home' }
    //     }
    //     return true
    // }

    // const isAuthenticated = await authStore.getUserLogged()
    // if (!isAuthenticated) return { name: 'login' }

    return true
})
