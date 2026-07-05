# REQ-USU-001: Roles ADMIN y USER

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** usuarios
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-FUE-002

## Descripción
Existen exactamente dos roles, en la misma tabla `users`: **ADMIN** (gestión interna: habilitar/deshabilitar fuentes, configuración general, panel administrativo) y **USER** (autoservicio: crea y gestiona sus propias búsquedas, destinos y notificaciones). Cuando en conversaciones se dijo "operador", se refería al ADMIN — no existe un tercer rol.

## Contexto / decisiones tomadas
- Cierra la duda #6 del borrador. El usuario confirmó (2026-07-04) que "operador" = el admin.
- Decisión previa (histórico #2): admin y autoservicio conviven en una tabla con campo `role`, no como sistemas separados.
- Si algún día aparece un caso de uso real para un tercer rol, extender el `CHECK` de `role` cuesta una línea en una migración futura; no se diseña por adelantado.

## Criterios de aceptación
- [ ] `users.role` con `CHECK (role IN ('ADMIN','USER'))`, default `USER`.
- [ ] Las operaciones de gestión de fuentes/configuración global solo son accesibles para ADMIN.
- [ ] Un usuario `INACTIVE` no puede iniciar sesión y el scheduler salta sus búsquedas.

## Notas / preguntas abiertas
- `users.status` (ACTIVE/INACTIVE) formalizado el 2026-07-04 tras la revisión de consistencia (era columna huérfana): INACTIVE = desactivación por ADMIN — bloquea login y pausa el monitoreo de sus búsquedas sin borrar nada.
