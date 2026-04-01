import { createI18n } from 'vue-i18n'
import { es } from '@/locales'

export default createI18n({
    legacy: false,
    allowComposition: true,
    locale: 'es',
    fallbackLocale: 'es',
    messages: { es },
})
