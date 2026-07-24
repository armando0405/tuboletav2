# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Frontend de TuBoleta v2 — SPA en Vue 3.5 + TypeScript con Vuetify 4, Vite, Pinia, Vue Router 5, axios y SweetAlert2. **Sin vue-i18n** (eliminado: proyecto monolingüe en español, textos directos en los componentes). Parte del monorepo `tuboletav2`; ver el `CLAUDE.md` de la raíz para la visión completa y `../requerimientos/` para los requerimientos formales (REQ-ARQ-005 define esta base; REQ-FE-* definen las pantallas por construir).

## Comandos

```bash
npm run dev         # dev server, http://localhost:7075
npm run build       # vue-tsc --noEmit && vite build
npm run typecheck   # vue-tsc --noEmit
npm run lint        # eslint . --fix
npm run preview     # preview de producción, puerto 5050 (choca con pgAdmin por defecto)
```

No hay script de tests configurado. Alias de path: `@` → `src/`.

## Estado actual (panel construido)

El tema **"Dark Operations"** (modo oscuro, acento índigo, Inter + JetBrains Mono — `../requerimientos/artefactos/diseno-frontend.md`) está aplicado y todas las pantallas del panel autoservicio están construidas (REQ-FE-001..005):

- **Dashboard** (`/home`, `views/dashboard/Index.vue`) — resumen real (búsquedas activas, eventos, no leídas, destinos) + accesos directos.
- **Búsquedas** (`views/searches/`) — listado con estado efectivo por par, crear/editar, pausa total (`/toggle`) y por proveedor, borrado lógico; + **eventos por búsqueda** (`SearchEvents.vue`).
- **Notificaciones** (`views/notifications/`) — inbox; marca-leída solo por acción explícita (REQ-NOT-003); badge de no leídas en el menú.
- **Destinos** (`views/destinations/`) — alta + activar/desactivar (sin borrado físico).
- **Admin de fuentes** (`views/admin/ProvidersAdmin.vue`) — solo ADMIN; deshabilitar (razón obligatoria) / habilitar.

La base heredada que se conservó: Login, layouts (blank/full), plugins, stores (auth/notify), `useNotify`, componentes reutilizables. El código de features del proyecto anterior fue eliminado (REQ-ARQ-005).

## Patrón de construcción de pantallas

**Documentado con ejemplo completo en `../requerimientos/artefactos/patron-frontend.md` — leerlo antes de crear una pantalla nueva.** Resumen del flujo:

```
Vista (views/) → Composable (composables/use<Entidad>.ts, estado module-level compartido)
             → Service (utils/services/, funciones tipadas por endpoint)
             → axios (plugins/axios.ts) → Backend
```

- `utils/endpoints/` — constantes de ruta por controlador del backend, re-exportadas como namespace en su `index.ts`
- `utils/services/` — wrappers tipados `api.get<ObjectListResponse<T>>(...)`; el envelope (`code` 0/-1, `msg`, `object`/`list`) espeja el backend y vive en `types/services/Responses.ts`
- `plugins/axios.ts` — instancia compartida: base URL de `VITE_API_URL` (= `/api`), `withCredentials`, interceptor que trata `code === -1` como error y redirige a login en 401. **Proxy de dev:** `vite.config.ts` reenvía `/api` → `http://localhost:8088`, así el SPA y la API quedan mismo-origen y la cookie de sesión viaja sin líos de CORS. (Sin ese proxy, `VITE_API_URL=/api` daría 404 contra el propio front.)
- Pantalla tipo CRUD: par `<Entidad>Header.vue`/`<Entidad>Body.vue` + composable con estado module-level + `TableDynamic`
- El guard de auth en `router/index.ts` está **activo**: sin sesión → login; rutas con `meta.requiresAdmin` (ej. `/admin/fuentes`) → home si el usuario no es ADMIN. Los composables limpian su estado module-level en `logout()`/`clearUser()` (evita fuga entre sesiones).

## Componentes base reutilizables

`ui-components/table/TableDynamic.vue` (tabla CRUD genérica, headers tipados `DataTableHeader[]`), `ui-components/loading/Loading.vue`, `ui-components/cards/FloatingCard.vue`, `components/shared/AppSnackbarQueue.vue` + notify store. Antes de construir algo custom, usar lo que Vuetify 4 ya ofrece (decisión REQ-ARQ-005).

## Convenciones

- Textos visibles en **español directo** en los componentes — no hay capa de traducciones (vue-i18n fue eliminado; la localización interna de Vuetify — `vuetify/locale` es — sí se conserva)
- Estados activo/inactivo: usar **booleanos** — la convención `'S'/'N'` era del proyecto anterior y fue limpiada de `vuetify.ts` y `TableDynamic.vue`
- Prettier: 4 espacios, 100 cols, sin punto y coma, comillas simples, un atributo por línea en templates
