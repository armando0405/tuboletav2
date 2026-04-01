<template>
    <v-floating-card :title="t('resolutions')">
        <resolutions-header />
    </v-floating-card>

    <v-floating-card
        v-if="showSubsourceCard"
        :title="t('relationResolutions')"
    >
        <resolutions-body />
    </v-floating-card>

    <div class="bottom-0 right-0 d-flex ga-2">
        <v-btn
            color="primary"
            to="/admin/subsource-types"
        >
            {{t('measurementSubsource')}}
        </v-btn>

        <v-btn
            color="primary"
            variant="flat"
            to="/admin/norm-sectors"
        >
            {{t('normSectors')}}
        </v-btn>
    </div>

    <v-loading :active="loading" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import ResolutionsHeader from '@/components/admin/Resolutions/ResolutionsHeader.vue'
import ResolutionsBody from '@/components/admin/Resolutions/ResolutionsBody.vue'
import { useResolutions } from '@/composables/admin/useResolutions'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, getListData, showSubsourceCard } = useResolutions()

onMounted(() => {
    getListData()
})
</script>
