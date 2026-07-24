# REQ-NOT-005: Gestión de credenciales de envío

**Estado:** implementado
**Tipo:** no-funcional
**Módulo:** notificaciones
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-NOT-002

## Descripción
Las credenciales del proveedor de correo (y de cualquier canal futuro) **jamás** se guardan en texto plano en el repositorio ni en archivos de configuración versionados: llegan por variables de entorno (o secret manager cuando exista). **Proveedor decidido (2026-07-04): SendGrid** — API key por variable de entorno `SENDGRID_API_KEY`, remitente verificado en la cuenta; el sender vive detrás de una interfaz (REQ-ARQ-004), así que migrar de proveedor no toca el flujo.

## Contexto / decisiones tomadas
- Lección directa de v1: el `application-dev.yml` tenía la contraseña de aplicación de Gmail en texto plano, versionada (deuda crítica #3 de `version-1-analisis.md`).
- Es requerimiento no-funcional aunque el proveedor se posponga: la regla aplica desde el primer commit que toque envío de correo.

## Criterios de aceptación
- [ ] Ningún secreto en archivos versionados (repo limpio ante un scan de secretos).
- [ ] La configuración de envío se lee de variables de entorno con nombres documentados.
