<template>
    <v-floating-card :title="t('measurementParameters')">
        <master-table />
    </v-floating-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import MasterTable from '@/components/admin/MeasurementParameters/MasterTable.vue'
const { t } = useI18n()
</script>
