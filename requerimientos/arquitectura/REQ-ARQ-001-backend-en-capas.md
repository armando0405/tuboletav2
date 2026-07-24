# REQ-ARQ-001: Arquitectura del backend en capas

**Estado:** implementado
**Tipo:** técnico
**Módulo:** arquitectura
**Última actualización:** 2026-07-06
**Relacionado con:** REQ-ARQ-002, REQ-ARQ-004

## Descripción
El backend usa la arquitectura en capas clásica de Spring: `api/controllers` (REST) → `service` (interfaces) + `service/impl` (implementaciones) → `repository` (Spring Data JPA), con `domain/entities` para las entidades JPA, `api/dtos` para los DTOs y `config` para configuración (security, persistence). Coincide con el layout objetivo ya documentado en `ESTRUCTURA_PROYECTO.md`.

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04): es la estructura con la que siempre ha trabajado (controlador → interfaz de servicio → implementación → repositorio) y la considera suficiente.
- **Arquitectura hexagonal descartada explícitamente**: "sería muy complejo, no le miraría el uso" — a esta escala agregaría ceremonia (puertos/adaptadores) sin beneficio real. No se revisita salvo que el proyecto cambie de escala.
- Los servicios se definen como interfaz + impl para facilitar tests y mantener el contrato separado de la implementación.

## Criterios de aceptación
- [ ] Todo controlador delega en una interfaz de servicio; ninguna lógica de negocio en controladores.
- [ ] Los repositorios solo se inyectan en servicios, nunca en controladores.
- [ ] La estructura de paquetes sigue el layout de `ESTRUCTURA_PROYECTO.md`.
