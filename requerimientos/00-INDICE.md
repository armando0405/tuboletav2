# Índice de requerimientos

| ID | Título | Módulo | Estado | Última actualización |
|----|--------|--------|--------|----------------------|
| REQ-ARQ-001 | Arquitectura del backend en capas | arquitectura | confirmado | 2026-07-04 |
| REQ-ARQ-002 | Convenciones de utils y envelope de respuestas | arquitectura | confirmado | 2026-07-04 |
| REQ-ARQ-003 | DTOs de entrada como records | arquitectura | confirmado | 2026-07-04 |
| REQ-ARQ-004 | Separación de responsabilidades por dominio | arquitectura | confirmado | 2026-07-04 |
| REQ-ARQ-005 | Base del frontend y vaciado del proyecto anterior | arquitectura | confirmado | 2026-07-04 |
| REQ-BD-001 | Esquema inicial v2 | base-datos | implementado | 2026-07-05 |
| REQ-BD-002 | Motor PostgreSQL 16 dockerizado | base-datos | implementado | 2026-07-05 |
| REQ-BD-003 | Política de migraciones | base-datos | implementado | 2026-07-05 |
| REQ-BUS-001 | Búsqueda multi-proveedor (N:M) | busquedas | confirmado | 2026-07-04 |
| REQ-BUS-002 | Normalización del término de búsqueda | busquedas | implementado | 2026-07-05 |
| REQ-BUS-003 | Coincidencia término ↔ resultados del proveedor | busquedas | implementado | 2026-07-05 |
| REQ-BUS-004 | Ciclo de vida de una búsqueda | busquedas | confirmado | 2026-07-04 |
| REQ-BUS-005 | Frecuencia de monitoreo elegida por el usuario | busquedas | confirmado | 2026-07-04 |
| REQ-DET-001 | Mecanismo de detección de cambios | deteccion | implementado | 2026-07-05 |
| REQ-DET-002 | Campos que disparan alerta de "modificado" | deteccion | implementado | 2026-07-05 |
| REQ-DET-003 | Ausencias transitorias y falsos "eliminados" | deteccion | implementado | 2026-07-05 |
| REQ-DET-004 | Scheduler de monitoreo | deteccion | confirmado | 2026-07-04 |
| REQ-DET-005 | Mecanismo de ejecución del scheduler — la BD es la cola | deteccion | confirmado | 2026-07-04 |
| REQ-FE-001 | Crear y gestionar búsquedas desde la web | frontend-autoservicio | borrador | 2026-07-04 |
| REQ-FE-002 | Listado de búsquedas con estado por proveedor | frontend-autoservicio | borrador | 2026-07-04 |
| REQ-FE-003 | Vista de eventos por búsqueda con destaque de novedades | frontend-autoservicio | borrador | 2026-07-04 |
| REQ-FE-004 | Centro de notificaciones en la web | frontend-autoservicio | borrador | 2026-07-04 |
| REQ-FE-005 | Gestión de destinos de notificación | frontend-autoservicio | borrador | 2026-07-04 |
| REQ-FUE-001 | Catálogo de proveedores con método de extracción configurable | fuentes | confirmado | 2026-07-04 |
| REQ-FUE-002 | Estado visible cuando una fuente se deshabilita | fuentes | confirmado | 2026-07-04 |
| REQ-NOT-001 | Destinos de notificación por usuario (1:N, reutilizables) | notificaciones | confirmado | 2026-07-04 |
| REQ-NOT-002 | Email como único canal activo | notificaciones | implementado | 2026-07-05 |
| REQ-NOT-003 | Centro de notificaciones in-app (inbox) | notificaciones | confirmado | 2026-07-04 |
| REQ-NOT-004 | Enrutamiento de alertas por tipo y canal | notificaciones | borrador | 2026-07-04 |
| REQ-NOT-005 | Gestión de credenciales de envío | notificaciones | implementado | 2026-07-05 |
| REQ-USU-001 | Roles ADMIN y USER | usuarios | confirmado | 2026-07-04 |
| REQ-USU-002 | Autenticación básica | usuarios | confirmado | 2026-07-04 |

## Fuera de alcance (descartes explícitos, no postergaciones)

- **Pagos / monetización**: descartado permanentemente por decisión del usuario (2026-07-04). No se contempla ni "a futuro".
- **Historial de método de extracción por fuente**: no se conserva registro de cuándo una fuente usó SCRAPER vs API; se reemplaza y ya (ver REQ-FUE-001).
