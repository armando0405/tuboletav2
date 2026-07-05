import { createRouter, createWebHistory } from 'vue-router'
import MainRoutes from './MainRoutes'
import AuthRoutes from './AuthRoutes'

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

// Guard de autenticación: STUB — la validación real se activará cuando
// exista el login contra el backend. Lógica prevista:
//   const authStore = useAuthStore()
//   const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
//   if (!requiresAuth) {
//       if (to.name === 'login' && (await authStore.getUserLogged())) return { name: 'home' }
//       return true
//   }
//   if (!(await authStore.getUserLogged())) return { name: 'login' }
router.beforeEach(async () => {
    return true
})
