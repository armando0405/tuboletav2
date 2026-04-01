<script setup>
import Icon from '../Icon.vue'

defineProps({
    item: {
        type: Object,
        default: () => ({}),
    },
    level: {
        type: Number,
        default: 0,
    },
})
</script>

<template>
    <v-tooltip
        location="right"
        :text="item.title"
    >
        <template #activator="{ props }">
            <v-list-item
                v-bind="props"
                :href="item.external ? item.to : undefined"
                :to="!item.external ? item.to : undefined"
                rounded
                class="mb-1 sidebar-item"
                color="primary"
                active-class="active-sidebar-item"
                :disabled="item.disabled"
                :target="item.external === true ? '_blank' : undefined"
                :exact="item.to === '/'"
            >
                <!-- Icon -->
                <template #prepend>
                    <Icon
                        :item="item.icon"
                        :level="level"
                    />
                </template>

                <!-- Title -->
                <v-list-item-title class="sidebar-title text-uppercase">
                    {{ item.title }}
                </v-list-item-title>

                <!-- Subtitle -->
                <v-list-item-subtitle
                    v-if="item.subCaption"
                    class="text-caption mt-n1 hide-menu text-uppercase"
                >
                    {{ item.subCaption }}
                </v-list-item-subtitle>
            </v-list-item>
        </template>
    </v-tooltip>
</template>
