<template>
    <router-view />
    <app-snackbar-queue />
</template>

<script setup lang="ts">
import AppSnackbarQueue from '@/components/shared/AppSnackbarQueue.vue'
import { RouterView } from 'vue-router'
</script>
