<script setup>
import NavItem from '../NavItem/index.vue'
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
    <!-- ---------------------------------------------- -->
    <!---Item Childern -->
    <!-- ---------------------------------------------- -->
    <v-list-group no-action>
        <!-- ---------------------------------------------- -->
        <!---Dropdown  -->
        <!-- ---------------------------------------------- -->
        <template #activator="{ props }">
            <v-list-item
                v-bind="props"
                :value="item.title"
                rounded
                class="mb-1"
            >
                <!---Icon  -->
                <template #prepend>
                    <icon
                        :item="item.icon"
                        :level="level"
                    />
                </template>
                <!---Title  -->
                <v-list-item-title class="mr-auto text-uppercase">
                    {{ item.title }}
                </v-list-item-title>
                <!---If Caption-->
                <v-list-item-subtitle
                    v-if="item.subCaption"
                    class="text-caption mt-n1 hide-menu text-uppercase"
                >
                    {{ item.subCaption }}
                </v-list-item-subtitle>
            </v-list-item>
        </template>
        <!-- ---------------------------------------------- -->
        <!---Sub Item-->
        <!-- ---------------------------------------------- -->
        <template
            v-for="(subitem, i) in item.children"
            :key="i"
        >
            <nav-collapse
                :item="subitem"
                v-if="subitem.children"
                :level="level + 1"
            />
            <nav-item
                :item="subitem"
                :level="level + 1"
                v-else
            ></nav-item>
        </template>
    </v-list-group>

    <!-- ---------------------------------------------- -->
    <!---End Item Sub Header -->
    <!-- ---------------------------------------------- -->
</template>
