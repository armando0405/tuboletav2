import { BlueTheme } from '@/theme/LightTheme'
import Swal from 'sweetalert2'
import type { App } from 'vue'

const { primary, accent } = BlueTheme.colors

const swalInstance = Swal.mixin({
    confirmButtonColor: primary,
    cancelButtonColor: accent,
    confirmButtonText: 'Aceptar',
    cancelButtonText: 'Cancelar',
    reverseButtons: true,
    buttonsStyling: true,
})

export default {
    install(app: App) {
        app.config.globalProperties.$swal = swalInstance

        if (typeof window !== 'undefined') {
            window.swal = swalInstance
        }
    },
}
