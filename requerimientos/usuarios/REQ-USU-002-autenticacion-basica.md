# REQ-USU-002: Autenticación básica

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** usuarios
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-NOT-005

## Descripción
Login con email + contraseña; la contraseña se almacena como hash BCrypt en `users.password_hash`. Verificación de email y recuperación de contraseña **no** se modelan todavía: se agregarán cuando exista envío de correo transaccional funcionando.

## Contexto / decisiones tomadas
- Decisión previa (histórico #4): mantener la autenticación mínima en esta etapa para no bloquear el modelo de datos.
- v1 no tenía autenticación de ningún tipo (endpoints abiertos) — cualquier endpoint operativo de v2 requiere sesión.

## Criterios de aceptación
- [ ] Contraseñas solo como hash BCrypt; nunca texto plano ni en logs.
- [ ] Sin tablas de tokens de verificación/reset en el esquema actual (se difieren a una migración futura).

## Notas / preguntas abiertas
- Definir mecanismo de sesión (JWT vs sesión de servidor) cuando se diseñe el backend — fuera del alcance de esta etapa.
- Nota de implementación: `spring-boot-starter-security` NO está aún en el `pom.xml` (hallazgo de revisión 2026-07-04) — se agrega al implementar este REQ (BCrypt viene con Spring Security).
