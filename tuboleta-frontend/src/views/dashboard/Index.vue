<template>
    <v-col cols="12">
        <v-table-dynamic
            :headers="headers"
            :items="users"
            selection-mode="single"
            :show-create="true"
            :show-edit="true"
            :show-delete="true"
            @create="crearUsuario"
            @edit="editarUsuario"
            @delete="eliminarUsuario"
            @selection-change="handleSelectionChange"
            class="elevation-1"
        />
    </v-col>
</template>

<script setup lang="ts">
/*Call Components*/
import { ref } from 'vue'

const users = ref([
    {
        id: 1,
        name: 'Juan',
        email: 'juan@email.com',
        status: 'Activo',
        created_at: '2026-03-01',
    },
    {
        id: 2,
        name: 'Pedro',
        email: 'pedro@email.com',
        status: 'Inactivo',
        created_at: '2026-03-02',
    },
    {
        id: 3,
        name: 'Maria',
        email: 'maria@email.com',
        status: 'Activo',
        created_at: '2026-03-03',
    },
    {
        id: 4,
        name: 'Carlos',
        email: 'carlos@email.com',
        status: 'Activo',
        created_at: '2026-03-04',
    },
    {
        id: 5,
        name: 'Ana',
        email: 'ana@email.com',
        status: 'Inactivo',
        created_at: '2026-03-05',
    },
    {
        id: 6,
        name: 'Luis',
        email: 'luis@email.com',
        status: 'Activo',
        created_at: '2026-03-06',
    },
    {
        id: 7,
        name: 'Sofia',
        email: 'sofia@email.com',
        status: 'Activo',
        created_at: '2026-03-07',
    },
    {
        id: 8,
        name: 'Miguel',
        email: 'miguel@email.com',
        status: 'Inactivo',
        created_at: '2026-03-08',
    },
    {
        id: 9,
        name: 'Laura',
        email: 'laura@email.com',
        status: 'Activo',
        created_at: '2026-03-09',
    },
    {
        id: 10,
        name: 'Diego',
        email: 'diego@email.com',
        status: 'Activo',
        created_at: '2026-03-10',
    },
    {
        id: 11,
        name: 'Paula',
        email: 'paula@email.com',
        status: 'Inactivo',
        created_at: '2026-03-11',
    },
    {
        id: 12,
        name: 'Roberto',
        email: 'roberto@email.com',
        status: 'Activo',
        created_at: '2026-03-12',
    },
    {
        id: 13,
        name: 'Fernanda',
        email: 'fernanda@email.com',
        status: 'Activo',
        created_at: '2026-03-13',
    },
    {
        id: 14,
        name: 'Javier',
        email: 'javier@email.com',
        status: 'Inactivo',
        created_at: '2026-03-14',
    },
    {
        id: 15,
        name: 'Valentina',
        email: 'valentina@email.com',
        status: 'Activo',
        created_at: '2026-03-15',
    },
])

/* -------------------------
   HEADERS
--------------------------*/

const headers = [
    {
        text: 'ID',
        key: 'id',
    },
    {
        text: 'Nombre',
        key: 'name',
    },
    {
        text: 'Email',
        key: 'email',
    },
    {
        text: 'Estado',
        key: 'status',
        filter: {
            type: 'select',
            items: ['Activo', 'Inactivo'],
        },
    },
    {
        text: 'Fecha creación',
        key: 'created_at',
        filter: {
            type: 'date',
        },
    },
]

function handleSelectionChange(items: typeof users.value) {
    console.log(items)
}
function crearUsuario() {
    console.log('Crear usuario')
}

function editarUsuario(item: unknown) {
    console.log('Editar usuario', item)
}

function eliminarUsuario(item: unknown) {
    console.log('Eliminar usuario', item)
}
</script>
