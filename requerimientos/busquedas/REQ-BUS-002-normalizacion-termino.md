# REQ-BUS-002: Normalización del término de búsqueda

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** busquedas
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-BUS-003
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Cada búsqueda guarda el término tal como lo escribió el usuario (`term`) y su forma normalizada (`term_normalized`): trim, minúsculas, colapso de espacios múltiples y **eliminación de tildes/diacríticos** ("Café Tacvba" ≡ "cafe tacvba"). Dos búsquedas del mismo usuario con términos equivalentes tras normalizar se consideran duplicadas: no se permite crear la segunda (la unicidad excluye búsquedas en estado DELETED).

## Contexto / decisiones tomadas
- Surge de la nota de voz (2026-07-04): "Fucks News " y "fucks news" deben ser la misma búsqueda; sin esto, un usuario acumula duplicados que scrapean lo mismo.
- Se implementa con índice único parcial `(user_id, term_normalized) WHERE status <> 'DELETED'` — así una búsqueda eliminada lógicamente no bloquea recrearla.
- El término normalizado es también el que se usa para la coincidencia contra resultados del proveedor (REQ-BUS-003).

## Criterios de aceptación
- [ ] Crear "Fucks News " cuando ya existe "fucks news" (mismo usuario) → rechazado con mensaje claro.
- [ ] Eliminar (lógico) una búsqueda y volver a crearla con el mismo término → permitido.

## Notas / preguntas abiertas
- (Resuelta 2026-07-04: el usuario confirmó que la normalización SÍ elimina tildes/diacríticos.)
