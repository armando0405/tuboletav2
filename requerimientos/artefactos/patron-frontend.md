# Patrón de construcción de pantallas del frontend

> Documentado el 2026-07-04 a partir del código del proyecto base (entidad "Resolutions"), **antes** del vaciado (REQ-ARQ-005). Este es el patrón a replicar cuando se construyan las pantallas de TuBoleta v2 (búsquedas, eventos, notificaciones, fuentes).

## Flujo de una petición

```
Vista (views/) → Composable (composables/) → Service (utils/services/) → axios (plugins/axios.ts) → Backend
```

El interceptor de `plugins/axios.ts` trata `code === -1` del envelope como error y redirige a login en 401. Los tipos del envelope (`ObjectResponse<T>`, `ObjectListResponse<T>` con `code`, `msg`, `object`/`list`) viven en `types/services/Responses.ts` y espejan el backend.

## Piezas por entidad (ejemplo: Resolutions)

### 1. Endpoints — `utils/endpoints/<grupo>Endpoints.ts`
Constantes de ruta agrupadas por controlador del backend; se re-exportan como namespace en `utils/endpoints/index.ts` (`export * as ADMIN from './adminEndpoints'`):
```ts
const BASE_API = '/admin'
const CONTROLLER_RESOLUTIONS = `${BASE_API}/Resolution`
const GET_RESOLUTIONS = `${CONTROLLER_RESOLUTIONS}/get-resolutions`
const POST_INSERT_OR_REPLACE_RESOLUTIONS = `${CONTROLLER_RESOLUTIONS}/insert-or-replace`
export { GET_RESOLUTIONS, POST_INSERT_OR_REPLACE_RESOLUTIONS }
```

### 2. Service — `utils/services/<grupo>Services.ts`
Funciones flecha tipadas, una por endpoint, devolviendo `Promise<AxiosResponse<Envelope<T>>>`; se exportan agrupadas en un objeto:
```ts
const getResolutions = (queryAll = true): Promise<AxiosResponse<ObjectListResponse<Resolutions>>> =>
    api.get<ObjectListResponse<Resolutions>>(ADMIN.GET_RESOLUTIONS, { params: { queryAll } })

export const adminService = { getResolutions, postInsertOrReplaceResolutions, ... }
```

### 3. Tipos — `types/services/<grupo>/<Entidad>.ts`
Interfaces del DTO (y variante `<Entidad>Request` cuando el payload difiere), re-exportadas desde `types/index.ts`.

### 4. Composable — `composables/<grupo>/use<Entidad>.ts`
**El estado vive a nivel de módulo (fuera de la función)** → se comparte entre todos los componentes que usan el composable (vista, header, body). La función exportada arma las operaciones:
```ts
const loading = ref(true)                 // estado compartido (module-level)
const listData = ref<Resolutions[]>([])
const selectedResolution = ref<Resolutions>({ ... })
const showSubsourceCard = computed(() => !!selectedResolution.value?.id)

export const useResolutions = () => {
    const getListData = async () => {
        try {
            loading.value = true
            const { data } = await getResolutions()
            listData.value = data?.list || []
        } catch (err) { console.error('Error al obtener datos', err) }
        finally { loading.value = false }
    }
    return { loading, listData, getListData, selectedResolution, showSubsourceCard }
}
```
Patrón de cada operación: `loading on → service → asignar `data.list`/`data.object` → catch log/notify → loading off`.

### 5. Vista — `views/<área>/<Entidad>.vue`
Composición delgada: tarjetas `v-floating-card` con el par Header/Body, `v-loading` global, y `onMounted(() => getListData())`. Sin lógica propia.

### 6. Componentes — `components/<área>/<Entidad>/<Entidad>Header.vue` + `<Entidad>Body.vue`
- **Header**: `v-table-dynamic` (componente propio `ui-components/table/TableDynamic.vue`: headers tipados `DataTableHeader[]`, `selection-mode`, botones create/edit emitiendo eventos) + `v-dialog` con el formulario del CRUD (campos Vuetify `density="compact"` `variant="outlined"`), guardado vía service + refresh + `window.swal.fire(...)` para feedback.
- **Body**: tabla/detalle dependiente de la selección del Header (comparten estado por el composable).

### 7. Ruta — `router/<Área>Route.ts`
`RouteRecordRaw[]` con hijos lazy (`component: () => import('@/views/...')`), anidado en `MainRoutes` (layout Full, requiresAuth). Entrada de menú en `layouts/full/vertical-sidebar/sidebarItem.ts`.

### 8. Textos
En español directo dentro de los componentes — **sin vue-i18n** (eliminado el 2026-07-04 por decisión del usuario: el proyecto es monolingüe y las traducciones no aportaban).

## Componentes/base reutilizables que SE CONSERVAN tras el vaciado

- `ui-components/table/TableDynamic.vue` — tabla genérica del CRUD (headers tipados, selección, filtros por tipo, botones crear/editar)
- `ui-components/loading/Loading.vue` (`v-loading`), `ui-components/cards/FloatingCard.vue` (`v-floating-card`)
- `components/shared/AppSnackbarQueue.vue` + `stores/notify.store.ts` + `composables/useNotify.ts` — cola global de notificaciones UI
- `stores/auth.store.ts`, `plugins/*` (axios/swal/vuetify), layouts blank/full completos, `utils/endpoints|services` de seguridad, tipos base (`Responses`, `LoginDTO`, `User`, `DynamicTable`)

## Notas para v2

- La convención `isActive: 'S' | 'N'` (switch con `true-value="S"`) era del backend del proyecto anterior — **en v2 usar booleanos reales** (el esquema Postgres usa BOOLEAN).
- Los componentes se registran con prefijo `v-` global (revisar `main.ts` al construir pantallas nuevas).
- Vuetify 4 trae tabla, dialog, forms, snackbar, etc. — usar lo que la librería ofrece antes de construir custom (decisión del usuario, REQ-ARQ-005).
