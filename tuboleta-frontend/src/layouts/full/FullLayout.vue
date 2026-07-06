<script setup lang="ts">
import { RouterView } from 'vue-router'
import MainView from './Main.vue'
</script>

<template>
    <v-locale-provider>
        <v-app>
            <main-view />

            <v-main>
                <v-container
                    fluid
                    class="page-wrapper"
                >
                    <router-view />
                </v-container>
            </v-main>
        </v-app>
    </v-locale-provider>
</template>
