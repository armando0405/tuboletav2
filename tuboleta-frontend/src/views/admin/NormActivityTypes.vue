<template>
    <v-floating-card :title="t('activityType')">
        <norm-activity-types-header />
    </v-floating-card>

    <v-floating-card
        v-if="showSourceTable"
        :title="t('relationActivityType')"
    >
        <norm-activity-types-body />
    </v-floating-card>

    <div class="bottom-0 right-0 d-flex ga-2">
        <v-btn
            color="primary"
            variant="flat"
            to="/admin/norm-sectors"
        >
            {{t('normSectors')}}
        </v-btn>
        <v-btn
            color="primary"
            variant="flat"
            to="/admin/limit-types"
        >
            {{t('limitTypes')}}
        </v-btn>
    </div>

    <v-loading :active="loading" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import NormActivityTypesBody from '@/components/admin/NormActivityTypes/NormActivityTypesBody.vue'
import NormActivityTypesHeader from '@/components/admin/NormActivityTypes/NormActivityTypesHeader.vue'
import { useTypeActivity } from '@/composables/admin/useNormActivityTypes'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, getListData, showSourceTable } = useTypeActivity()

onMounted(() => {
    getListData()
})
</script>
