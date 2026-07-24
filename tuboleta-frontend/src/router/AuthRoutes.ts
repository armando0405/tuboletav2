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
        {
            name: 'forgot-password',
            path: '/recuperar',
            component: () => import('@/views/auth/ForgotPassword.vue'),
        },
        {
            name: 'reset-password',
            path: '/reset-password',
            component: () => import('@/views/auth/ResetPassword.vue'),
        },
    ],
}

export default AuthRoutes
