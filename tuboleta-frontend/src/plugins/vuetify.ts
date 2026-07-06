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
            //variant: 'flat',
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
