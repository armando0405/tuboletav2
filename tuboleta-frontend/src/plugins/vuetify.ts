import { createVuetify } from 'vuetify'
import '@mdi/font/css/materialdesignicons.css'
import * as directives from 'vuetify/directives'
import { TuboletaDarkTheme } from '@/theme/DarkTheme'
import { es } from 'vuetify/locale'

const inputDefaults = {
    density: 'compact',
    variant: 'outlined',
    //hideDetails: 'auto',
}

export default createVuetify({
    directives,
    locale: {
        locale: 'es',
        messages: { es },
    },
    theme: {
        defaultTheme: 'tuboletaDark',
        themes: {
            tuboletaDark: TuboletaDarkTheme,
        },
    },
    defaults: {
        VBtn: {
            // Relleno sólido para que el CTA primario (índigo) resalte sobre las
            // superficies oscuras del modal (los botones que deben ser sutiles
            // fijan variant="text" explícitamente).
            variant: 'flat',
            color: 'primary',
            rounded: 'lg',
        },
        VCard: {
            rounded: 'md',
        },
        VTextField: {
            ...inputDefaults,
            rounded: 'lg',
        },
        VSelect: {
            ...inputDefaults,
            // Cierra la lista al elegir una opción (incluso en multiple): antes
            // el usuario tenía que clicar fuera para cerrarla — se sentía roto.
            menuProps: { closeOnContentClick: true },
        },
        VAutocomplete: {
            ...inputDefaults,
        },
        VCombobox: {
            ...inputDefaults,
        },
        VCheckbox: {
            hideDetails: 'auto',
            color: 'primary',
        },
        VRadioGroup: {
            hideDetails: 'auto',
            color: 'primary',
        },
        VSwitch: {
            hideDetails: 'auto',
            color: 'primary',
        },
        VFileInput: {
            ...inputDefaults,
        },
        VTextarea: {
            ...inputDefaults,
            rows: 3,
            noResize: true,
        },
        VTooltip: {
            location: 'top',
        },
    },
})
