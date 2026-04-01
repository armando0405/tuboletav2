<template>
    <v-overlay
        :model-value="activeLoading"
        :contained="!props.full"
        persistent
        :z-index="9999"
        class="loading-primary-overlay"
        @wheel.prevent
    >
        <div class="loading-content">
            <div
                class="spinner"
                :style="{ animationDuration: `${2 / props.speed}s` }"
            >
                <div
                    class="dot dot1"
                    :style="{ animationDuration: `${2 / props.speed}s, 3s` }"
                />
                <div
                    class="dot dot2"
                    :style="{ animationDuration: `${2 / props.speed}s, 3s` }"
                />
                <div
                    class="dot dot3"
                    :style="{ animationDuration: `${2 / props.speed}s, 3s` }"
                />
                <div
                    class="dot dot4"
                    :style="{ animationDuration: `${2 / props.speed}s, 3s` }"
                />
            </div>
            <p class="loading-message">
                <slot>Cargando, por favor espere...</slot>
            </p>
        </div>
    </v-overlay>
</template>

<script setup>
import { computed, onBeforeUnmount } from 'vue'

const props = defineProps({
    active: {
        type: Boolean,
        default: false,
    },
    full: {
        type: Boolean,
        default: false,
    },
    speed: {
        type: Number,
        default: 1,
    },
})
onBeforeUnmount(() => {
    document.body.style.overflow = ''
    document.body.style.cursor = ''
})
const activeLoading = computed(() => {
    if (props.active) {
        document.body.style.overflow = 'hidden'
        document.body.style.cursor = 'wait'
        return true
    }
    document.body.style.overflow = ''
    document.body.style.cursor = ''
    return false
})
</script>

<style scoped>
.loading-primary-overlay :deep(.v-overlay__scrim) {
    background: rgba(255, 255, 255, 0.35);
    backdrop-filter: blur(4px);
    opacity: 1;
}

.loading-primary-overlay :deep(.v-overlay__content) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
}

.loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

.spinner {
    width: 60px;
    height: 60px;
    position: relative;
    animation: sk-rotate 2s infinite linear;
    margin-bottom: 20px;
}

.dot {
    width: 40%;
    height: 40%;
    position: absolute;
    border-radius: 50%;
    animation:
        sk-bounce 2s infinite ease-in-out,
        color-change 3s infinite alternate;
}

.dot1 {
    top: 0;
    left: 50%;
    transform: translateX(-50%);
}
.dot2 {
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    animation-delay: -1s, 0.5s;
}
.dot3 {
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    animation-delay: -0.5s, 1s;
}
.dot4 {
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    animation-delay: -1.5s, 1.5s;
}

@keyframes sk-rotate {
    100% {
        transform: rotate(360deg);
    }
}

@keyframes sk-bounce {
    0%,
    100% {
        transform: scale(0);
    }
    50% {
        transform: scale(1);
    }
}

@keyframes color-change {
    0% {
        background-color: #4cafef;
    }
    25% {
        background-color: #ff4081;
    }
    50% {
        background-color: #ffeb3b;
    }
    75% {
        background-color: #66bb6a;
    }
    100% {
        background-color: #ab47bc;
    }
}

.loading-message {
    font-size: 1.6rem;
    font-weight: bold;
    color: #111;
    text-shadow: 1px 1px 3px rgba(255, 255, 255, 0.6);
}

.loading-primary-overlay :deep(.v-overlay__content) {
    position: fixed;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}
</style>
