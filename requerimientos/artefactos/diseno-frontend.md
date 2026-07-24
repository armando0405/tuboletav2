# Dirección visual del frontend — "Dark Operations"

> Elegida por el usuario el 2026-07-04 entre 3 propuestas (Claro Profesional / **Dark Operations** / Vibrante Eventos), generadas con la skill `ui-ux-pro-max`. Aplica a todas las pantallas de autoservicio (REQ-FE-001..004). Stack: Vue 3 + Vuetify 4.

## Concepto

Centro de monitoreo en modo oscuro primario (referentes: Linear, Grafana): el usuario entra a revisar el estado de sus búsquedas y alertas, muchas veces de noche. Superficies elevadas sobre negro suave, un solo acento índigo para lo interactivo, y verde/rojo reservados exclusivamente para estados. Sobrio, denso en datos pero respirado.

**El modo oscuro es el tema primario y único al inicio** (modo claro queda como excepción futura, no se diseña ahora).

## Tokens de color (tema Vuetify custom)

| Token | Valor | Uso |
|---|---|---|
| `background` | `#0A0A0C` | Fondo de la app (negro suave — **nunca #000000 puro**) |
| `surface` | `#121216` | Cards, sidebar, topbar |
| `surface-elevated` | `#1A1E2F` | Modales, menús, hover de cards |
| `primary` | `#5E6AD2` | Índigo — botones, links, foco, elementos interactivos |
| `success` | `#22C55E` | Búsqueda/fuente activa, evento nuevo, envío exitoso |
| `error` | `#EF4444` | Evento eliminado/cancelado, fallo de envío, fuente deshabilitada |
| `warning` | `#F59E0B` | Evento modificado, estados intermedios |
| `on-surface` | `#E0E0E6` | Texto principal (contraste ≥ 7:1 sobre background) |
| `on-surface-variant` | `#9CA3AF` | Texto secundario (≥ 4.5:1 sobre surface) |
| `outline` | `#2A2E3F` | Bordes y divisores (visibles pero sutiles) |

Reglas: los colores semánticos (success/error/warning) **siempre acompañados de icono o texto** — nunca color solo. Chips de estado: `● ACTIVO`, `▲ NUEVO`, `✕ ELIMINADO`.

## Tipografía

- **Inter** — toda la UI (títulos con weight 600–700, cuerpo 400, labels 500). Base 16px, line-height 1.5.
- **JetBrains Mono** — datos tabulares: fechas, horas, contadores, frecuencias, IDs. Figuras tabulares para que las columnas no salten.
- Escala: 12 (solo metadatos) / 14 / 16 / 18 / 24 / 32.

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');
```

## Efectos e interacción

- **Elevación por color, no por sombra**: las superficies más importantes son más claras (`surface` → `surface-elevated`), bordes `outline` sutiles. Sin sombras dramáticas.
- Transiciones 150–300ms, ease-out al entrar / ease-in al salir; respetar `prefers-reduced-motion`.
- Hover/press: cambio de fondo a `surface-elevated` + borde `primary` tenue; glow mínimo solo en foco (`box-shadow: 0 0 0 2px` índigo al 40%).
- Estados leído/no leído del inbox: no leída = borde izquierdo índigo + fondo ligeramente elevado + punto; leída = plana.
- Iconos: **Material Design Icons** (`@mdi/font`, ya instalado) o SVG — jamás emojis como iconos. Un solo estilo (outline) y tamaño por jerarquía.

## Guía por pantalla

- **Dashboard / Mis búsquedas (REQ-FE-002)**: grid de cards por búsqueda; cada card muestra término, chips de proveedor con estado **efectivo** (activo/pausado/fuente deshabilitada), frecuencia en mono (`12h`), contador de eventos y badge de novedades.
- **Eventos por búsqueda (REQ-FE-003)**: lista/tabla densa; evento nuevo/cambiado destacado (borde izquierdo success/warning + badge) mientras su notificación esté no leída; REMOVED en texto atenuado con icono, no solo color.
- **Inbox (REQ-FE-004)**: lista cronológica con distinción leído/no leído descrita arriba; contador de no leídas como badge en el nav. El badge baja al marcar leídas (individualmente o "marcar todas") — **nunca** se limpia solo por visitar el inbox (REQ-NOT-003: el dashboard usa el mismo `read_at` para destacar novedades).
- **Formulario nueva búsqueda (REQ-FE-001)**: labels visibles (no placeholder-only), preview del término normalizado, selección de frecuencia como `v-btn-toggle` de 4 opciones (6h/12h/24h/48h), errores debajo del campo.

## Anti-patrones (evitar)

- Negro puro `#000000` de fondo; blanco puro `#FFFFFF` en texto masivo (usar `#E0E0E6`).
- Neón/glow decorativo por todas partes — el glow es solo feedback de foco.
- Más de un acento: el índigo es el único color interactivo; verde/rojo/ámbar son SOLO estado.
- Emojis como iconos; mezclar sets de iconos; sombras tipo material claro sobre fondo oscuro.

## Implementación en Vuetify (cuando se construya)

Definir tema custom en `src/plugins/vuetify.ts` (`defaultTheme: 'tuboletaDark'`) con los tokens de arriba en `colors` + `variables`; reemplaza al tema actual de la plantilla (`BlueTheme`, definido en `src/theme/LightTheme.ts`). Revisar contraste de los componentes Vuetify por defecto sobre las superficies definidas.
