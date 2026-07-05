# REQ-ARQ-004: Separación de responsabilidades por dominio

**Estado:** confirmado
**Tipo:** técnico
**Módulo:** arquitectura
**Última actualización:** 2026-07-04
**Requiere:** REQ-ARQ-001
**Relacionado con:** REQ-DET-004, REQ-NOT-003

## Descripción
Cada clase tiene una responsabilidad clara y acotada a su dominio: el servicio de scraping scrapea, el de detección de cambios compara, el de notificaciones decide qué notificar, y el envío por canal (email, futuros Telegram/WhatsApp) es una pieza aparte por canal. Los **schedulers solo orquestan**: disparan servicios según la programación, sin lógica de negocio dentro ("que no sea una mezcolanza, que se sepa muy bien dónde va cada cosa").

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04). Es además la lección directa de v1, donde `SnapshotService` mezclaba scraping+hash+comparación+persistencia y el scheduler contenía lógica de análisis de resultados (ver `version-1-analisis.md`).
- Separación esperada (nombres ilustrativos, se afinan al implementar): `ScrapingService`/extractores por proveedor → `ChangeDetectionService` → `NotificationService` (crea el hecho notificable) → `EmailSenderService` (y un sender por canal futuro), orquestados por un scheduler delgado.
- Encaja con el modelo de datos: `notifications` (hecho) vs `notifications_log` (entrega por canal) ya separan esos conceptos en BD.

## Criterios de aceptación
- [ ] Ningún scheduler contiene lógica de negocio (solo selección de trabajo pendiente + invocación de servicios).
- [ ] El envío por canal está detrás de una interfaz; agregar un canal = nueva implementación, sin tocar el flujo de notificación.
- [ ] Ninguna clase de servicio cruza dos dominios (ej. scraping que envía correos).
