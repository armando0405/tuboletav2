import type { RouteRecordRaw } from 'vue-router'

const AdminRoutes: RouteRecordRaw[] = [
    {
        path: '/admin',
        name: 'Admin',
        redirect: '/admin/client-extra-info',
        children: [
            {
                path: 'client-extra-info',
                name: 'ClientExtraInfo',
                component: () => import('@/views/admin/ClientExtraInfo.vue'),
            },
            {
                path: 'account-extra-info',
                name: 'AccountExtraInfo',
                component: () => import('@/views/admin/AccountExtraInfo.vue'),
            },
            {
                path: 'measurement-parameters',
                name: 'MeasurementParameters',
                component: () => import('@/views/admin/MeasurementParameters.vue'),
            },
            {
                path: 'laboratories',
                name: 'Laboratories',
                component: () => import('@/views/admin/Laboratories.vue'),
            },
            {
                path: 'resolutions',
                name: 'Resolutions',
                component: () => import('@/views/admin/Resolutions.vue'),
            },
            {
                path: 'period',
                name: 'Period',
                component: () => import('@/views/admin/Period.vue'),
            },
            {
                path: 'parameter-rates',
                name: 'ParameterRates',
                component: () => import('@/views/admin/ParameterRates.vue'),
            },
            {
                path: 'declaration-format',
                name: 'DeclarationFormat',
                component: () => import('@/views/admin/DeclarationFormat.vue'),
            },
            {
                path: 'limit-types',
                name: 'LimitTypes',
                component: () => import('@/views/admin/LimitTypes.vue'),
            },
            {
                path: 'subsource-types',
                name: 'SubsourceTypes',
                component: () => import('@/views/admin/SubsourceTypes.vue'),
            },
            {
                path: 'declaration-period',
                name: 'DeclarationPeriod',
                component: () => import('@/views/admin/DeclarationPeriod.vue'),
            },
            {
                path: 'norm-sectors',
                name: 'NormSectors',
                component: () => import('@/views/admin/NormSectors.vue'),
            },
            {
                path: 'norm-activity-types',
                name: 'NormActivityTypes',
                component: () => import('@/views/admin/NormActivityTypes.vue'),
            },
            {
                path: 'measurement-source-type',
                name: 'MeasurementSourceType',
                component: () => import('@/views/admin/MeasurementSourceType.vue'),
            },
            {
                path: 'header-parameters',
                name: 'HeaderParameters',
                component: () => import('@/views/admin/HeaderParameters.vue'),
            },
        ],
    },
]

export default AdminRoutes
