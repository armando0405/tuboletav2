import type { ThemeTypes } from '@/types/themeTypes/ThemeType'

// "Dark Operations" — dirección visual elegida por el usuario el 2026-07-04
// (requerimientos/artefactos/diseno-frontend.md). Modo oscuro primario y
// único por ahora; el índigo (`primary`) es el ÚNICO acento interactivo,
// success/warning/error son EXCLUSIVAMENTE indicadores de estado.
const TuboletaDarkTheme: ThemeTypes = {
    name: 'tuboletaDark',
    dark: true,
    variables: {
        'border-color': '#2A2E3F',
        'carousel-control-size': 10,
    },
    colors: {
        background: '#0A0A0C',
        surface: '#121216',
        'surface-elevated': '#1A1E2F',
        primary: '#5E6AD2',
        // Un solo acento interactivo (regla del artefacto): secondary/info
        // reutilizan el mismo índigo en vez de introducir un segundo tono.
        secondary: '#5E6AD2',
        info: '#5E6AD2',
        success: '#22C55E',
        warning: '#F59E0B',
        error: '#EF4444',
        // Acento neutro para acciones secundarias (p.ej. botón "Cancelar" de
        // SweetAlert2): gris de texto secundario, no un segundo color vivo.
        accent: '#9CA3AF',
        'on-surface': '#E0E0E6',
        'on-surface-variant': '#9CA3AF',
        outline: '#2A2E3F',

        // Alias heredados de la plantilla, mapeados a los tokens de arriba
        // para que el scss ya existente (sidebar, cards, inputs) siga
        // funcionando sin reescribir cada regla.
        textPrimary: '#E0E0E6',
        textSecondary: '#9CA3AF',
        borderColor: '#2A2E3F',
        borderLight: '#2A2E3F',
        inputBorder: '#2A2E3F',
        containerBg: '#121216',
        hoverColor: '#1A1E2F',
        darkText: '#E0E0E6',
        lightText: '#9CA3AF',
        grey100: '#1A1E2F',
        muted: '#9CA3AF',
        // Tintes oscuros de los colores de estado, para fondos de chips/hover.
        lightprimary: '#20244A',
        lightsecondary: '#20244A',
        lightsuccess: '#132A1D',
        lighterror: '#2E1417',
        lightinfo: '#20244A',
        lightwarning: '#2E2410',
    },
}

export { TuboletaDarkTheme }
