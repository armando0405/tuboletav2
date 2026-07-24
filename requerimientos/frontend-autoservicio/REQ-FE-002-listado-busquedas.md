# REQ-FE-002: Listado de búsquedas con estado por proveedor

**Estado:** borrador
**Tipo:** funcional
**Módulo:** frontend-autoservicio
**Última actualización:** 2026-07-04
**Requiere:** REQ-BUS-001
**Relacionado con:** REQ-FUE-002
**Artefactos relacionados:** artefactos/diseno-frontend.md, artefactos/patron-frontend.md

## Descripción
El usuario ve sus búsquedas y, dentro de cada una, el estado por proveedor: activo, pausado por él, o fuente deshabilitada por el administrador. El estado mostrado es el **efectivo** (combinación de la pausa del par y el estado global del proveedor) — nunca debe parecer que algo se monitorea cuando no es así.

## Contexto / decisiones tomadas
- Surge de la nota de voz: "él tiene consultas de tuboleta, también de ticketmaster... quiere ver las de ticketmaster, entra a verlas, y ahí está el estado".
- Conecta con REQ-FUE-002 (borrador): cuando una fuente se deshabilita, este listado es donde el usuario lo ve reflejado.

## Notas / preguntas abiertas
- Definir presentación (agrupado por búsqueda con chips por proveedor, o tabla plana) en el diseño del módulo.
