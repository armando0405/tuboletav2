<template>
    <v-floating-card :title="t('normSectors')">
        <norm-sectors-header />
    </v-floating-card>

    <v-floating-card
        v-if="showSourceTable"
        :title="t('relationNormSectors')"
    >
        <norm-sectors-body />
    </v-floating-card>

    <div class="bottom-0 right-0 d-flex ga-2">
        <v-btn
            color="primary"
            to="/admin/resolutions"
        >
            {{ t('resolutions') }}
        </v-btn>
        <v-btn
            color="primary"
            to="/admin/norm-activity-types"
        >
            {{ t('activityType') }}
        </v-btn>
    </div>

    <v-loading :active="loading" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import NormSectorsHeader from '@/components/admin/NormSectors/NormSectorsHeader.vue'
import NormSectorsBody from '@/components/admin/NormSectors/NormSectorsBody.vue'
import { useNormSectors } from '@/composables/admin/useNormSectors'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, getListData, showSourceTable } = useNormSectors()

onMounted(() => {
    getListData()
})
</script>
