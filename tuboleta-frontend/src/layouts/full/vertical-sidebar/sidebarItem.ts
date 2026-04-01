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

const sidebarItem: menu[] = [
    {
        title: 'GESTIÓN AMBIENTAL',
        to: '/',
    },

    {
        title: 'ADMINISTRACIÓN',
        children: [
            {
                title: 'Tipos Fuentes Medición',
                to: '/admin/measurement-source-type',
            },
            {
                title: 'Tipos Subfuentes',
                to: '/admin/subsource-types',
            },
            {
                title: 'Tipo Actividad Norma',
                to: '/admin/norm-activity-types',
            },
            {
                title: 'Tipo Limite',
                to: '/admin/limit-types',
            },
            {
                title: 'Parametros de la Aplicacion',
                to: '/admin/parameter-rates',
            },
            {
                title: 'Periodos',
                to: '/admin/period',
            },
            {
                title: 'Periodos de Declaración',
                to: '/admin/declaration-period',
            },
            {
                title: 'Parámetros Medición',
                to: '/admin/measurement-parameters',
            },
            {
                title: 'Formato Declaracion',
                to: '/admin/declaration-format',
            },
            {
                title: 'Laboratorios',
                to: '/admin/laboratories',
            },
            {
                title: 'Información Adicional Cliente',
                to: '/admin/client-extra-info',
            },
            {
                title: 'Información Adicional de Cuentas',
                to: '/admin/account-extra-info',
            },
            {
                title: 'Parámetros de encabezado',
                to: '/admin/header-parameters',
            },
            {
                title: 'Sectores Normativos',
                to: '/admin/norm-sectors',
            },
            {
                title: 'Resolución',
                to: '/admin/resolutions',
            },
        ],
    },
    {
        title: 'AUTODECLARACIÓN',
        children: [
            {
                title: 'Formulario. Autodeclaracion',
                to: '/autodeclaration/form-autodeclaration',
            },
            {
                title: 'Consulta de Autodeclaraciones',
                to: '/autodeclaration/query-autodeclaration',
            },
        ],
    },

    {
        title: 'CARACTERIZACIÓN',
        children: [
            {
                title: 'Regis. Sitios de Muestreo',
                to: '/characterization/characterizations',
            },
            {
                title: 'Presentación Masiva',
                to: '/characterization/mass-submission',
            },
        ],
    },
]
export default sidebarItem
