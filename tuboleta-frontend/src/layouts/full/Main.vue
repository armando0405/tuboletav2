<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useDisplay } from 'vuetify'
import sidebarItems from './vertical-sidebar/sidebarItem'
import type { menu } from './vertical-sidebar/sidebarItem'
import NavGroup from './vertical-sidebar/NavGroup/index.vue'
import NavItem from './vertical-sidebar/NavItem/index.vue'
import Logo from './logo/Logo.vue'
import ProfileDD from './vertical-header/ProfileDD.vue'
import NavCollapse from './vertical-sidebar/NavCollapse/NavCollapse.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useNotifications } from '@/composables/notifications/useNotifications'

const authStore = useAuthStore()
const { unreadCount, getUnreadCount } = useNotifications()

// Filtra las entradas `adminOnly` para usuarios no-ADMIN (solo visual — la
// protección real de la ruta vive en el guard de router/index.ts) e inyecta
// el badge de no leídas (REQ-FE-004/REQ-NOT-003) en la entrada
// "Notificaciones" usando los campos `chip*` ya declarados en `menu` (nunca
// conectados a un render hasta ahora).
const sidebarMenu = computed<menu[]>(() =>
    sidebarItems
        .filter((group) => !group.adminOnly || authStore.isAdmin)
        .map((group) => ({
            ...group,
            children: group.children
                ?.filter((child) => !child.adminOnly || authStore.isAdmin)
                .map((child) =>
                    child.to === '/notificaciones' && unreadCount.value > 0
                        ? { ...child, chip: String(unreadCount.value), chipColor: 'error' }
                        : child,
                ),
        })),
)

const { mdAndDown } = useDisplay()
const sDrawer = ref<boolean>(true)
onMounted(() => {
    sDrawer.value = !mdAndDown.value // hide on mobile, show on desktop
    void getUnreadCount()
})
watch(mdAndDown, (val: boolean) => {
    sDrawer.value = !val
})
</script>

<template>
    <!------Sidebar-------->
    <v-navigation-drawer
        elevation="0"
        app
        class="leftSidebar"
        :width="300"
        v-model="sDrawer"
    >
        <!---Logo part -->
        <div class="pa-5">
            <Logo />
        </div>
        <!-- ---------------------------------------------- -->
        <!---Navigation -->
        <!-- ---------------------------------------------- -->
        <div>
            <perfect-scrollbar class="scrollnavbar">
                <v-list class="pa-6">
                    <!---Menu Loop -->
                    <template
                        v-for="(item, index) in sidebarMenu"
                        :key="index"
                    >
                        <nav-group
                            :item="item"
                            v-if="item.header"
                        />

                        <nav-collapse
                            class="leftPadding"
                            :item="item"
                            :level="0"
                            v-else-if="item.children"
                        />

                        <nav-item
                            :item="item"
                            v-else
                            class="leftPadding"
                        />
                    </template>
                </v-list>
            </perfect-scrollbar>
        </div>
    </v-navigation-drawer>
    <!------Header-------->
    <v-app-bar
        app
        elevation="0"
        height="70"
        class="top-header"
    >
        <div class="d-flex align-center justify-space-between w-100">
            <div>
                <v-btn
                    class="hidden-lg-and-up ms-md-3 ms-sm-5 ms-3 text-muted"
                    @click="sDrawer = !sDrawer"
                    icon
                    size="small"
                >
                    <v-icon icon="mdi-menu" />
                </v-btn>
            </div>
            <div>
                <profile-d-d />
            </div>
        </div>
    </v-app-bar>
</template>
