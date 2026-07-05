import type { Component } from 'vue'

export interface menu {
    header?: string
    title?: string
    icon?: Component
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
}

// Menú de TuBoleta v2 — se poblará al construir los módulos
// (búsquedas, notificaciones, fuentes; ver requerimientos/frontend-autoservicio/)
const sidebarItem: menu[] = [
    {
        title: 'INICIO',
        children: [
            {
                title: 'Dashboard',
                to: '/',
            },
        ],
    },
]
export default sidebarItem
