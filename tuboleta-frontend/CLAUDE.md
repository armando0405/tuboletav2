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

## Estado actual (post-vaciado 2026-07-04)

Este proyecto nació de una plantilla de otro proyecto; todas sus features fueron **eliminadas** y quedó solo el esqueleto funcional: Login, dashboard (shell con datos demo), Error404, layouts (blank/full con sidebar/topbar), plugins, stores (auth/notify), `useNotify`, componentes reutilizables y la capa de servicios de seguridad. Las pantallas de TuBoleta (búsquedas, eventos, inbox de notificaciones, gestión de destinos) están por construir según los REQ-FE-*. La dirección visual ya está definida: **"Dark Operations"** (modo oscuro primario, acento índigo, Inter + JetBrains Mono) — tokens y guía por pantalla en `../requerimientos/artefactos/diseno-frontend.md`.

## Patrón de construcción de pantallas

**Documentado con ejemplo completo en `../requerimientos/artefactos/patron-frontend.md` — leerlo antes de crear una pantalla nueva.** Resumen del flujo:

```
Vista (views/) → Composable (composables/use<Entidad>.ts, estado module-level compartido)
             → Service (utils/services/, funciones tipadas por endpoint)
             → axios (plugins/axios.ts) → Backend
```

- `utils/endpoints/` — constantes de ruta por controlador del backend, re-exportadas como namespace en su `index.ts`
- `utils/services/` — wrappers tipados `api.get<ObjectListResponse<T>>(...)`; el envelope (`code` 0/-1, `msg`, `object`/`list`) espeja el backend y vive en `types/services/Responses.ts`
- `plugins/axios.ts` — instancia compartida: base URL de `VITE_API_URL`, `withCredentials`, interceptor que trata `code === -1` como error y redirige a login en 401
- Pantalla tipo CRUD: par `<Entidad>Header.vue`/`<Entidad>Body.vue` + composable + `TableDynamic`
- El guard de auth en `router/index.ts` es un **stub** (deja pasar todo); la lógica prevista está comentada ahí mismo

## Componentes base reutilizables

`ui-components/table/TableDynamic.vue` (tabla CRUD genérica, headers tipados `DataTableHeader[]`), `ui-components/loading/Loading.vue`, `ui-components/cards/FloatingCard.vue`, `components/shared/AppSnackbarQueue.vue` + notify store. Antes de construir algo custom, usar lo que Vuetify 4 ya ofrece (decisión REQ-ARQ-005).

## Convenciones

- Textos visibles en **español directo** en los componentes — no hay capa de traducciones (vue-i18n fue eliminado; la localización interna de Vuetify — `vuetify/locale` es — sí se conserva)
- Estados activo/inactivo: usar **booleanos** — la convención `'S'/'N'` era del proyecto anterior y fue limpiada de `vuetify.ts` y `TableDynamic.vue`
- Prettier: 4 espacios, 100 cols, sin punto y coma, comillas simples, un atributo por línea en templates
