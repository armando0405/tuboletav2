# REQ-BD-002: Motor PostgreSQL 16 dockerizado

**Estado:** confirmado
**Tipo:** técnico
**Módulo:** base-datos
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-BD-001

## Descripción
La base de datos de v2 es PostgreSQL 16, corriendo en Docker vía el `docker-compose.yml` de la raíz del repo (junto con pgAdmin como interfaz visual). Queda descartado MySQL.

## Contexto / decisiones tomadas
Cierra la duda #2 del borrador (¿Postgres o MySQL?). Se resuelve como decisión de facto con justificación:
- El esquema confirmado ya usa `JSONB` (`providers.config`, `events.raw_json`) y `TIMESTAMPTZ`; MySQL no tiene equivalentes con la misma ergonomía (su `TIMESTAMP` tiene conversión UTC implícita y límite 2038).
- En Docker, la carga administrativa de Postgres y MySQL es prácticamente igual; la fama de "Postgres es pesado" corresponde a otra época (bare-metal), no a esta escala.
- El `docker-compose.yml` con Postgres 16 + pgAdmin ya existe y funciona.

## Criterios de aceptación
- [ ] `docker-compose up -d` levanta Postgres 16 con las credenciales del archivo `env`.
- [ ] El backend conecta y aplica las migraciones sin dependencia de otro motor.
