# REQ-BUS-003: Coincidencia término ↔ resultados del proveedor

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** busquedas
**Última actualización:** 2026-07-04
**Requiere:** REQ-BUS-002

## Descripción
De los resultados que devuelve el buscador del proveedor, solo se guardan (y por tanto solo se notifican) aquellos cuyo título contiene el término normalizado completo como subcadena, comparando en forma normalizada (misma normalización de REQ-BUS-002, incluida la eliminación de tildes). El resto se descarta antes de tocar la tabla `events`.

**El filtro aplica solo a eventos NUEVOS** (external_id no registrado). Un evento ya registrado se sigue procesando aunque su título cambie y deje de contener el término: eso genera un CHANGED de título (que es la información real), nunca un REMOVED silencioso por dejar de "coincidir" (cierra el caso borde detectado en la revisión de consistencia).

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04) entre cuatro opciones: notificar todo / **contiene la frase** / todas las palabras / configurable por búsqueda. Eligió "contiene la frase" como balance entre precisión y no perder eventos.
- Motivación: los buscadores de los proveedores pueden ser laxos y devolver resultados vagamente relacionados; sin filtro local, cada resultado espurio genera una alerta falsa.
- Ejemplo: buscar "fucks news" atrapa "FUCKS NEWS EN BOGOTÁ" pero no "Festival de noticias rock".

## Criterios de aceptación
- [ ] Un resultado cuyo título no contiene el término normalizado no genera fila en `events` ni notificación.
- [ ] La comparación es insensible a mayúsculas/espacios extra (misma normalización de REQ-BUS-002).

## Notas / preguntas abiertas
- Si a futuro se necesita más flexibilidad (ej. artistas con variantes de nombre), evolucionar a modo de coincidencia configurable por búsqueda — sería un REQ nuevo, no cambiar el default.
