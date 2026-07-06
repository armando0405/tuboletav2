# REQ-BUS-004: Ciclo de vida de una búsqueda

**Estado:** implementado
**Tipo:** funcional
**Módulo:** busquedas
**Última actualización:** 2026-07-06

## Descripción
`searches.status` tiene tres estados: `ACTIVE` (se monitorea), `INACTIVE` (el usuario la pausó; el scheduler la salta) y `DELETED` (eliminación lógica). Nunca se hace `DELETE` físico de una búsqueda: su histórico de eventos y notificaciones se conserva.

## Contexto / decisiones tomadas
- Heredado del diseño original de v2 y ratificado: el borrado lógico preserva la trazabilidad (eventos, cambios y notificaciones referencian la búsqueda).
- La pausa a nivel de búsqueda (`INACTIVE`) es distinta de la pausa por par proveedor (`search_providers.is_active`): la primera detiene todo, la segunda solo un proveedor.

## Criterios de aceptación
- [ ] No existe ningún endpoint/operación que haga DELETE físico de `searches`.
- [ ] El scheduler ignora búsquedas `INACTIVE` y `DELETED`.
