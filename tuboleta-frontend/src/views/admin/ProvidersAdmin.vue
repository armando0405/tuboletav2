<template>
    <v-col
        cols="12"
        class="position-relative"
    >
        <v-loading :active="loading && providers.length === 0" />

        <providers-admin-header />

        <v-card
            variant="flat"
            class="providers-card"
        >
            <providers-admin-table />
        </v-card>

        <provider-disable-dialog />
    </v-col>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import ProvidersAdminHeader from '@/components/admin/ProvidersAdminHeader.vue'
import ProvidersAdminTable from '@/components/admin/ProvidersAdminTable.vue'
import ProviderDisableDialog from '@/components/admin/ProviderDisableDialog.vue'
import { useProvidersAdmin } from '@/composables/admin/useProvidersAdmin'

const { loading, providers, getProviders } = useProvidersAdmin()

onMounted(() => {
    getProviders()
})
</script>

<style scoped>
.providers-card {
    background: rgb(var(--v-theme-surface));
    border: 1px solid rgb(var(--v-theme-outline));
    overflow-x: auto;
}
</style>
