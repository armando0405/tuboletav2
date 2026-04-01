<script setup lang="ts">
import { onMounted, ref, shallowRef, watch } from 'vue'
import { useDisplay } from 'vuetify'
import sidebarItems from './vertical-sidebar/sidebarItem'
import NavGroup from './vertical-sidebar/NavGroup/index.vue'
import NavItem from './vertical-sidebar/NavItem/index.vue'
import Logo from './logo/Logo.vue'
import ProfileDD from './vertical-header/ProfileDD.vue'
import NavCollapse from './vertical-sidebar/NavCollapse/NavCollapse.vue'
const sidebarMenu = shallowRef(sidebarItems)

const { mdAndDown } = useDisplay()
const sDrawer = ref<boolean>(true)
// Variable exported to template
void sidebarMenu.value
onMounted(() => {
    sDrawer.value = !mdAndDown.value // hide on mobile, show on desktop
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
