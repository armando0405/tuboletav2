<template>
    <div class="mb-6">
        <div class="d-flex align-center ga-2 mb-1">
            <v-icon
                icon="mdi-shield-crown-outline"
                color="primary"
            />
            <h1 class="text-h4 font-weight-bold mb-0">Fuentes</h1>
            <v-chip
                size="small"
                variant="tonal"
                color="primary"
            >
                Solo administrador
            </v-chip>
        </div>
        <p class="text-body-2 text-medium-emphasis mb-0">
            Habilita o deshabilita proveedores. Al deshabilitar una fuente, el scheduler deja de
            monitorearla de inmediato y se notifica a todos los usuarios con búsquedas afectadas.
        </p>
    </div>
</template>
