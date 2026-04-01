<template>
    <v-floating-card :title="t('measurementSource')">
        <measurement-source-type-header />
    </v-floating-card>

    <v-loading :active="loading" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import MeasurementSourceTypeHeader from '@/components/admin/MeasurementSourceType/MeasurementSourceTypeHeader.vue'
import { useMeasurementSourceType } from '@/composables'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, getListData } = useMeasurementSourceType()

onMounted(() => {
    getListData()
})
</script>
