# REQ-ARQ-002: Convenciones de utils y envelope de respuestas

**Estado:** implementado
**Tipo:** técnico
**Módulo:** arquitectura
**Última actualización:** 2026-07-06
**Requiere:** REQ-ARQ-001

## Descripción
El paquete `utils/` existente se conserva como hogar formal de todo lo reutilizable: `constants` (ErrorCode, ErrorMessage), `exception` (excepciones custom + `GlobalExceptionHandler`) y `response` (envelopes). Regla general: si algo se va a usar varias veces, vive en `utils`. Toda respuesta REST usa el envelope estándar: `ObjectResponse<T>` (objeto único) u `ObjectListResponse<T>` (listas), con `code = 0` → éxito + mensaje de satisfacción, `code = -1` → fallo + mensaje de error.

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04): "quiero dejar algo más formalizado, algo reutilizable... el frontend valida si funcionó o falló con el código 0/-1". El frontend ya está acoplado a este contrato (interceptor de axios trata `code === -1` como error).
- Envelopes adicionales solo se crean si un servicio realmente lo exige ("para servicios más complejos se puede estructurar otro, pero de momento utilicemos eso").
- Los errores nunca se manejan ad hoc en controladores: se lanzan las excepciones custom y el `GlobalExceptionHandler` centraliza la respuesta (convención ya documentada en `CLAUDE.md`).

## Criterios de aceptación
- [ ] Ningún endpoint devuelve un DTO "pelado" ni estructuras de respuesta inventadas.
- [ ] Nuevas excepciones se agregan al `GlobalExceptionHandler`, con mensajes en `ErrorMessage`.
- [ ] Código utilitario duplicado en dos lugares → se extrae a `utils`.
