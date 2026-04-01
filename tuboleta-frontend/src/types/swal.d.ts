import type Swal from 'sweetalert2'

declare global {
    interface Window {
        swal: typeof Swal
    }
}

declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        $swal: typeof Swal
    }
}
