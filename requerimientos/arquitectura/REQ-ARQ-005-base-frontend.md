# REQ-ARQ-005: Base del frontend y vaciado del proyecto anterior

**Estado:** confirmado
**Tipo:** técnico
**Módulo:** arquitectura
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-FE-001, REQ-FE-002, REQ-FE-003, REQ-FE-004
**Artefactos relacionados:** artefactos/patron-frontend.md

## Descripción
El frontend conserva el stack y la arquitectura del proyecto base: Vue 3 + TypeScript, **Vuetify 4** (usar lo que la librería ya ofrece antes de construir custom), axios para servicios, Pinia, y el patrón `utils/endpoints` + `utils/services` + composables + componentes reutilizables, con layouts blank/full. **Sin vue-i18n**: el proyecto es solo en español, las traducciones no aportan (decisión del usuario 2026-07-04) — los textos visibles van en español directo en los componentes; la dependencia, el plugin y `locales/` fueron eliminados. El código de **features** del proyecto anterior (vistas/componentes/composables/tipos de laboratorios, resoluciones, autodeclaraciones) **se elimina** — no tiene nada que ver con TuBoleta; antes de borrarlo, el patrón de construcción de pantallas queda documentado en `artefactos/patron-frontend.md`.

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04): "el frontend que está ahí prácticamente no tiene nada que ver con lo nuestro, pero la arquitectura de cómo quedó armado sí... se sigue usando Vuetify, axios, TypeScript... el endpoint en un lado, el service en el otro". Confirmó ejecutar el vaciado ya (no diferirlo).
- Se conserva el esqueleto funcional: Login, dashboard (shell), Error404, layouts, plugins, stores auth/notify, `useNotify`, componentes shared/ui (TableDynamic, Loading, FloatingCard), servicios/endpoints de seguridad y tipos base.
- La dirección visual quedó definida: **"Dark Operations"** (modo oscuro primario, acento índigo, Inter + JetBrains Mono), elegida por el usuario entre 3 propuestas → detalle completo en `artefactos/diseno-frontend.md`.

## Criterios de aceptación
- [ ] Tras el vaciado, `npm run typecheck` y `npm run lint` pasan sin errores.
- [ ] No queda ninguna vista/componente/tipo/endpoint del dominio del proyecto anterior.
- [ ] El patrón de pantallas quedó documentado en `artefactos/patron-frontend.md` antes del borrado.
