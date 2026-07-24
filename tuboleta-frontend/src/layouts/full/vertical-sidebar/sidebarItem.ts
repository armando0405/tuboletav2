import type { Component } from 'vue'

export interface menu {
    header?: string
    title?: string
    icon?: Component | string
    to?: string
    chip?: string
    chipColor?: string
    chipBgColor?: string
    chipVariant?: string
    chipIcon?: string
    children?: menu[]
    disabled?: boolean
    type?: string
    subCaption?: string
    external?: boolean
    // Oculta la entrada para usuarios no-ADMIN. Es solo un filtro visual: la
    // protección real de la ruta vive en el guard de router/index.ts.
    adminOnly?: boolean
}

// Menú de TuBoleta v2 (REQ-FE-001..005, REQ-FUE-001/002).
const sidebarItem: menu[] = [
    {
        title: 'PANEL',
        children: [
            {
                title: 'Dashboard',
                icon: 'mdi-view-dashboard-outline',
                to: '/',
            },
            {
                title: 'Mis búsquedas',
                icon: 'mdi-magnify',
                to: '/busquedas',
            },
            {
                title: 'Notificaciones',
                icon: 'mdi-bell-outline',
                to: '/notificaciones',
            },
            {
                title: 'Destinos',
                icon: 'mdi-email-outline',
                to: '/destinos',
            },
            {
                title: 'Mi cuenta',
                icon: 'mdi-account-cog-outline',
                to: '/cuenta',
            },
        ],
    },
    {
        title: 'ADMINISTRACIÓN',
        adminOnly: true,
        children: [
            {
                title: 'Fuentes',
                icon: 'mdi-database-cog-outline',
                to: '/admin/fuentes',
                adminOnly: true,
            },
            {
                title: 'Frecuencias',
                icon: 'mdi-timer-cog-outline',
                to: '/admin/frecuencias',
                adminOnly: true,
            },
            {
                title: 'Usuarios',
                icon: 'mdi-account-group-outline',
                to: '/admin/usuarios',
                adminOnly: true,
            },
        ],
    },
]
export default sidebarItem
