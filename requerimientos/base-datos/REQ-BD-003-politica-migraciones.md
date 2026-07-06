# REQ-BD-003: Política de migraciones

**Estado:** implementado
**Tipo:** técnico
**Módulo:** base-datos
**Última actualización:** 2026-07-04
**Requiere:** REQ-BD-002
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Flyway, dentro del backend, es el único mecanismo que aplica cambios de esquema (automáticamente al arrancar). La definición **conceptual** del esquema vive en esta carpeta de requerimientos; el `.sql` es el artefacto derivado, enlazado (no copiado) desde `artefactos/esquema-bd.md`. Hibernate no gestiona esquema: `spring.jpa.hibernate.ddl-auto` debe ser `validate`.

## Contexto / decisiones tomadas
- Cierra la duda #1 del borrador. El usuario aclaró (2026-07-04) que su objeción era de **visibilidad** (que el esquema no quede enterrado como detalle del backend), no de mecanismo. Se resuelve documentando aquí y enlazando, sin renunciar a la garantía "backend arriba = esquema correcto".
- Hallazgo del revisor: `ddl-auto: update` estaba activo encima de Flyway, permitiendo a Hibernate alterar el esquema silenciosamente. Se corrige a `validate`.
- No se usa copia del `.sql` en artefactos: las copias derivan y terminan mintiendo.
- **Regla de editabilidad**: `V1__init.sql` es editable mientras no exista un despliegue compartido; toda edición implica recrear la BD local (`docker-compose down -v`). A partir del primer despliegue compartido, todo cambio va en una migración nueva `V{n}__...`.

## Criterios de aceptación
- [ ] `application.yaml` con `ddl-auto: validate`.
- [ ] La regla de editabilidad está en el header de `V1__init.sql` y en `CLAUDE.md`.
- [ ] `artefactos/esquema-bd.md` enlaza a la migración y mapea tabla → REQ.
