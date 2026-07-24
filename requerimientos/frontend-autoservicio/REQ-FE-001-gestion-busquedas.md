# REQ-FE-001: Crear y gestionar búsquedas desde la web

**Estado:** borrador
**Tipo:** funcional
**Módulo:** frontend-autoservicio
**Última actualización:** 2026-07-04
**Requiere:** REQ-BUS-001, REQ-BUS-002, REQ-BUS-005
**Relacionado con:** REQ-FE-005
**Artefactos relacionados:** artefactos/diseno-frontend.md, artefactos/patron-frontend.md

## Descripción
El usuario autoservicio crea una búsqueda desde la web: escribe el término (con feedback de cómo quedó normalizado y validación de duplicados), elige en qué proveedores monitorearla, con qué frecuencia (6/12/24/48 h) y **a cuáles de sus destinos de notificación va** (ver REQ-FE-005; si no tiene destinos, el flujo lo lleva a crear uno). Puede editarla, pausarla (total o por proveedor) y eliminarla (borrado lógico).

## Contexto / decisiones tomadas
- Surge de la nota de voz: "¿cómo hacemos para indicar qué buscar?... simplemente agregar [el término] al buscador".
- El flujo debe hacer visible lo que decidieron REQ-BUS-002/003: qué término se guardó y qué criterio de coincidencia se aplica, para que el usuario entienda qué va a atrapar su búsqueda.

## Notas / preguntas abiertas
- Definir el flujo exacto de UI (¿wizard o formulario único?) cuando se diseñe el módulo frontend.
- ¿El usuario ve una vista previa de resultados actuales del proveedor antes de confirmar la búsqueda? (deseable, por definir).
