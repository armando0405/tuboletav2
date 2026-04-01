const AuthRoutes = {
    path: '/',
    component: () => import('@/layouts/blank/BlankLayout.vue'),
    meta: {
        requiresAuth: false,
    },
    children: [
        {
            name: 'login',
            path: '/login',
            component: () => import('@/views/auth/Login.vue'),
        },
    ],
}

export default AuthRoutes
