<template>
    <div class="position-relative">
        <v-loading :active="loading" />

        <h1 class="text-h5 font-weight-bold mb-1">Usuarios</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">
            Administra los usuarios: activa/desactiva cuentas y cambia roles.
        </p>

        <v-card
            rounded="lg"
            variant="flat"
            border
        >
            <v-table hover>
                <thead>
                    <tr>
                        <th>Usuario</th>
                        <th>Correo</th>
                        <th>Rol</th>
                        <th>Estado</th>
                        <th class="text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <tr
                        v-for="u in users"
                        :key="u.id"
                    >
                        <td class="py-2">
                            {{ u.name }}
                            <v-chip
                                v-if="u.id === currentUserId"
                                size="x-small"
                                color="primary"
                                variant="tonal"
                                class="ms-1"
                            >
                                tú
                            </v-chip>
                        </td>
                        <td class="text-medium-emphasis">{{ u.email }}</td>
                        <td>
                            <v-chip
                                size="small"
                                :color="u.role === 'ADMIN' ? 'primary' : 'default'"
                                variant="tonal"
                            >
                                {{ u.role }}
                            </v-chip>
                        </td>
                        <td>
                            <v-chip
                                size="small"
                                :color="u.status === 'ACTIVE' ? 'success' : 'error'"
                                variant="tonal"
                                :prepend-icon="
                                    u.status === 'ACTIVE'
                                        ? 'mdi-check-circle-outline'
                                        : 'mdi-cancel'
                                "
                            >
                                {{ u.status === 'ACTIVE' ? 'Activo' : 'Inactivo' }}
                            </v-chip>
                        </td>
                        <td class="text-right">
                            <div class="d-flex justify-end ga-2 flex-wrap py-1">
                                <v-btn
                                    size="small"
                                    variant="tonal"
                                    :color="u.role === 'ADMIN' ? 'default' : 'primary'"
                                    :disabled="u.id === currentUserId || submitting"
                                    @click="toggleRole(u)"
                                >
                                    {{ u.role === 'ADMIN' ? 'Quitar admin' : 'Hacer admin' }}
                                </v-btn>
                                <v-btn
                                    size="small"
                                    variant="tonal"
                                    :color="u.status === 'ACTIVE' ? 'error' : 'success'"
                                    :disabled="u.id === currentUserId || submitting"
                                    @click="setStatus(u, u.status !== 'ACTIVE')"
                                >
                                    {{ u.status === 'ACTIVE' ? 'Desactivar' : 'Activar' }}
                                </v-btn>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </v-table>

            <p
                v-if="!loading && users.length === 0"
                class="text-body-2 text-medium-emphasis text-center pa-8 mb-0"
            >
                No hay usuarios para mostrar.
            </p>
        </v-card>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAdminUsers } from '@/composables/admin/useAdminUsers'
import { useAuthStore } from '@/stores/auth.store'
import type { AdminUser } from '@/types/services/AdminUser'

const { loading, users, submitting, getUsers, setStatus, setRole } = useAdminUsers()
const authStore = useAuthStore()
const currentUserId = authStore.user?.id ?? -1

const toggleRole = (u: AdminUser) => setRole(u, u.role === 'ADMIN' ? 'USER' : 'ADMIN')

onMounted(getUsers)
</script>
