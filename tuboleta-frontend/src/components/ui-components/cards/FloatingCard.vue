<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
    title: string
}>()

const isClicked = ref(false)

function animateButton() {
    isClicked.value = true

    setTimeout(() => {
        isClicked.value = false
    }, 300)
}
</script>

<template>
    <v-card class="mb-10 py-4 position-relative">
        <v-card-title
            class="floating-button text-h3 font-weight-bold text-uppercase bg-primary elevation-2"
            @click="animateButton"
            :class="{ clicked: isClicked }"
        >
            {{ title }}
        </v-card-title>

        <v-card-text>
            <slot />
        </v-card-text>
    </v-card>
</template>

<style scoped>
.floating-button {
    position: absolute;
    top: -20px;
    left: 16px;
    color: white;
    padding: 8px 20px;
    border-radius: 5px;
    cursor: pointer;
    transition: all 0.3s ease;
    user-select: none;
    font-weight: 600;
    text-align: center;
    font-size: 1.25rem;
    max-width: calc(100% - 32px);
    white-space: normal;
    word-break: break-word;
}

/* Hover efecto */
.floating-button:hover {
    transform: translateY(-3px) scale(1.05);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35);
    background: linear-gradient(135deg, #478ed1, #42a5f5);
}

/* Animación click */
.floating-button.clicked {
    animation: click-bounce 0.3s forwards;
}

/* Keyframes click-bounce */
@keyframes click-bounce {
    0% {
        transform: scale(1) translateY(0);
    }
    50% {
        transform: scale(1.2) translateY(-10px);
    }
    100% {
        transform: scale(1) translateY(0);
    }
}

/* Responsive */
@media (max-width: 768px) {
    .floating-button {
        font-size: 1rem;
        padding: 6px 16px;
        top: -16px;
        left: 12px;
        max-width: calc(100% - 24px);
    }
}

@media (max-width: 480px) {
    .floating-button {
        font-size: 0.875rem;
        padding: 4px 12px;
        top: -14px;
        left: 8px;
        max-width: calc(100% - 16px);
    }
}
</style>
