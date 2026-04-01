<template>
    <v-floating-card :title="t('measurementSubsource')">
        <subsource-types />
    </v-floating-card>

    <v-floating-card
        v-if="showSourceTable"
        :title="t('relationSubsource')"
    >
        <source-by-subsource />
    </v-floating-card>

    <div class="bottom-0 right-0 d-flex ga-2">
        <v-btn
            color="primary"
            variant="flat"
            to="/admin/measurement-source-type"
        >
            {{t('measurementSource')}}
        </v-btn>

        <v-btn
            color="primary"
            variant="flat"
            to="/admin/resolutions"
        >
            {{t('resolutions')}}
        </v-btn>
    </div>

    <v-loading :active="loading" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import SubsourceTypes from '@/components/admin/SubsourceTypes/SubsourceTypes.vue'
import SourceBySubsource from '@/components/admin/SubsourceTypes/SourceBySubsource.vue'
import { useSubsourceTypes } from '@/composables'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, getListData, showSourceTable } = useSubsourceTypes()

onMounted(() => {
    getListData()
})
</script>
