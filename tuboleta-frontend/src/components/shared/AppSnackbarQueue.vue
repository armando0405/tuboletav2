<template>
    <v-snackbar-queue
        v-model="notifyStore.messages"
        location="bottom end"
        :total-visible="5"
        timeout="4000"
        :collapsed="true"
        display-strategy="overflow"
        min-width="300"
        max-width="400"
    >
        <template #text="{ item }">
            {{ item.text }}
        </template>

        <template #actions="{ props }">
            <v-btn
                icon="$close"
                size="small"
                variant="text"
                v-bind="props"
            />
        </template>
    </v-snackbar-queue>
</template>

<script setup lang="ts">
import { useNotifyStore } from '@/stores/notify.store'

const notifyStore = useNotifyStore()
</script>
