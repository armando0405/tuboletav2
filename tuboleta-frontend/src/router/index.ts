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

// Guard de autenticación: ahora ACTIVO (antes stub) — ya conectamos
// login/logout/me reales en el auth store, dejarlo como stub habría sido
// inconsistente. Si el store ya tiene usuario (misma sesión SPA) no se
// vuelve a llamar a /me en cada navegación; solo se valida contra el backend
// cuando el store está vacío (carga en frío / recarga de página). El
// interceptor de axios (401) se encarga de limpiar el store y redirigir si
// la sesión expira entre medio.
router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)

    if (!requiresAuth) {
        if (
            to.name === 'login' &&
            !authStore.isAuthenticated &&
            (await authStore.getUserLogged())
        ) {
            return { name: 'home' }
        }
        return true
    }

    if (!authStore.isAuthenticated && !(await authStore.getUserLogged())) {
        return { name: 'login' }
    }

    if (to.matched.some((r) => r.meta.requiresAdmin) && !authStore.isAdmin) {
        return { name: 'home' }
    }

    return true
})
