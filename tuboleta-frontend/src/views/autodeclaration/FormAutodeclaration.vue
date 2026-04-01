<template>
    <v-container fluid>
        <VLoandingPrimary
            :active="loading"
            :full="true"
        />

        <v-row>
            <v-col cols="12">
                <v-card>
                    <v-card-title class="text-h4 font-weight-bold">
                        Pruebas SweetAlert2
                    </v-card-title>

                    <v-card-text>
                        <p class="text-h6 text-grey-darken-1">
                            Panel para probar todas las funciones
                        </p>

                        <v-row
                            class="mt-4"
                            dense
                        >
                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="success"
                                    @click="alertSuccess"
                                >
                                    Success
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="error"
                                    @click="alertError"
                                >
                                    Error
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="warning"
                                    @click="alertWarning"
                                >
                                    Warning
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="info"
                                    @click="alertInfo"
                                >
                                    Info
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="primary"
                                    @click="confirmDelete"
                                >
                                    Confirmación
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="secondary"
                                    @click="promptInput"
                                >
                                    Input Prompt
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="primary"
                                    @click="showLoader"
                                >
                                    Loader
                                </v-btn>
                            </v-col>

                            <v-col
                                cols="12"
                                md="3"
                            >
                                <v-btn
                                    block
                                    color="primary"
                                    @click="showToast"
                                >
                                    Toast
                                </v-btn>
                            </v-col>
                        </v-row>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const loading = ref(false)

/* SUCCESS */
const alertSuccess = () => {
    window.swal.fire('Guardado', 'Registro almacenado correctamente', 'success')
}

/* ERROR */
const alertError = () => {
    window.swal.fire('Error', 'Algo salió mal', 'error')
}

/* WARNING */
const alertWarning = () => {
    window.swal.fire('Advertencia', 'Esto puede causar problemas', 'warning')
}

/* INFO */
const alertInfo = () => {
    window.swal.fire('Información', 'Este es un mensaje informativo', 'info')
}

/* CONFIRM */
const confirmDelete = async () => {
    const result = await window.swal.fire({
        title: '¿Eliminar registro?',
        text: 'Esta acción no se puede revertir',
        icon: 'warning',
        showCancelButton: true,
    })

    if (result.isConfirmed) {
        window.swal.fire('Eliminado', 'El registro fue eliminado', 'success')
    }
}

/* PROMPT INPUT */
const promptInput = async () => {
    const { value } = await window.swal.fire({
        title: 'Ingrese su nombre',
        input: 'text',
        inputPlaceholder: 'Nombre...',
    })

    if (value) {
        window.swal.fire(`Hola ${value}`)
    }
}

/* LOADER */
const showLoader = () => {
    window.swal.fire({
        title: 'Procesando',
        text: 'Espere un momento...',
        allowOutsideClick: false,
        didOpen: () => {
            window.swal.showLoading()
        },
    })

    setTimeout(() => {
        window.swal.close()
    }, 2000)
}

/* TOAST */
const showToast = () => {
    window.swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: 'Actualizado correctamente',
        showConfirmButton: false,
        timer: 2500,
    })
}

onMounted(() => {
    loading.value = true
    setTimeout(() => {
        loading.value = false
    }, 1500)
})
</script>
