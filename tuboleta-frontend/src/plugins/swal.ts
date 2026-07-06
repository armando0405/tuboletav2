import { TuboletaDarkTheme } from '@/theme/DarkTheme'
import Swal from 'sweetalert2'
import type { App } from 'vue'

const { primary, accent } = TuboletaDarkTheme.colors
const { 'surface-elevated': surfaceElevated, 'on-surface': onSurface } = TuboletaDarkTheme.colors

const swalInstance = Swal.mixin({
    confirmButtonColor: primary,
    cancelButtonColor: accent,
    confirmButtonText: 'Aceptar',
    cancelButtonText: 'Cancelar',
    reverseButtons: true,
    buttonsStyling: true,
    background: surfaceElevated,
    color: onSurface,
})

export default {
    install(app: App) {
        app.config.globalProperties.$swal = swalInstance

        if (typeof window !== 'undefined') {
            window.swal = swalInstance
        }
    },
}
