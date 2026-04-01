<script setup lang="ts">
import { RouterView } from 'vue-router'
import MainView from './Main.vue'
</script>

<template>
    <v-locale-provider>
        <v-app class="bg-grey-lighten-4">
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
