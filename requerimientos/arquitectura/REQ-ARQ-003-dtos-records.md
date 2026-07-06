# REQ-ARQ-003: DTOs de entrada como records

**Estado:** implementado
**Tipo:** técnico
**Módulo:** arquitectura
**Última actualización:** 2026-07-06
**Requiere:** REQ-ARQ-001

## Descripción
Los DTOs que reciben información del frontend (request bodies, en `api/dtos`) se implementan como **records de Java**: la información que llega del frontend no se modifica, así que la inmutabilidad del record es el ajuste natural y elimina boilerplate.

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04): "la información que se recibe del frontend no se va a modificar; utiliza record, creo que es el mejor uso". Java 21 lo soporta de sobra.
- Los records de entrada llevan las anotaciones de validación (`@NotNull`, `@Size`, etc.) en sus componentes; la validación la dispara `@Valid` en el controlador y los errores los formatea el `GlobalExceptionHandler` (REQ-ARQ-002).
- Las entidades JPA **no** son records (JPA requiere mutabilidad/proxies); el envelope de respuesta (`ObjectResponse`) se mantiene como clase existente.

## Criterios de aceptación
- [ ] Todo request body nuevo es un record con validaciones en sus componentes.
- [ ] Ningún record de entrada se muta después de recibido (no hay setters posibles).
