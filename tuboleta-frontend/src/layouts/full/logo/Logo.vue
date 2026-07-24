<template>
    <router-link
        to="/"
        class="tb-logo"
    >
        <v-icon
            icon="mdi-radar"
            color="primary"
            size="28"
        />
        <span class="tb-logo__text">
            Tu<span class="tb-logo__accent">Boleta</span>
        </span>
    </router-link>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.tb-logo {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    text-decoration: none;
}
.tb-logo__text {
    font-size: 1.35rem;
    font-weight: 700;
    letter-spacing: 0.5px;
    color: rgb(var(--v-theme-on-surface));
}
.tb-logo__accent {
    color: rgb(var(--v-theme-primary));
}
</style>
