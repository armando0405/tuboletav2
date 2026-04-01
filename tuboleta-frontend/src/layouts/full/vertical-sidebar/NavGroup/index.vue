<script setup>
defineProps({
    item: {
        type: Object,
        default: () => ({}),
    },
})
</script>

<template>
    <v-list-subheader
        color="darkText"
        class="smallCap text-uppercase text-subtitle-2 mt-5 font-weight-bold text-uppercase"
    >
        {{ item.header }}
    </v-list-subheader>
</template>
