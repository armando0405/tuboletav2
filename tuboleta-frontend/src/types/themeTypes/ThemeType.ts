export type ThemeTypes = {
    name: string
    dark: boolean
    variables?: object
    colors: {
        // Tokens "Dark Operations" (requerimientos/artefactos/diseno-frontend.md)
        background?: string
        surface?: string
        'surface-elevated'?: string
        primary?: string
        secondary?: string
        info?: string
        success?: string
        accent?: string
        warning?: string
        error?: string
        'on-surface'?: string
        'on-surface-variant'?: string
        outline?: string
        // Alias heredados de la plantilla (consumidos por scss/ existente):
        // se mantienen para no romper estilos ya escritos contra estos nombres.
        lightprimary?: string
        lightsecondary?: string
        lightsuccess?: string
        lighterror?: string
        lightinfo?: string
        lightwarning?: string
        textPrimary?: string
        textSecondary?: string
        borderColor?: string
        borderLight?: string
        hoverColor?: string
        inputBorder?: string
        containerBg?: string
        darkText?: string
        lightText?: string
        grey100?: string
        muted?: string
    }
}
