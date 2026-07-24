<script setup>
defineProps({
    item: {
        type: [String, Object, Function],
        default: undefined,
    },
    level: {
        type: Number,
        default: 0,
    },
})
</script>

<template>
    <!-- Material Design Icons (@mdi/font, Dark Operations): string tipo "mdi-*" -->
    <v-icon
        v-if="typeof item === 'string'"
        :icon="item"
        :size="level > 0 ? 14 : 20"
        class="iconClass"
    />
    <template v-else-if="item">
        <component
            :is="item"
            :size="level > 0 ? 14 : 20"
            stroke-width="1.5"
            class="iconClass"
        ></component>
    </template>
</template>
