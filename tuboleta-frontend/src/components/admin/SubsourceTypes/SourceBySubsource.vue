<template>
    <div>
        <v-table-dynamic
            :headers="headers"
            :items="listSourceData"
            :show-create="true"
            :show-edit="true"
            selection-mode="single"
            @create="openModal()"
            @edit="openModal"
        />
    </div>

    <v-dialog
        v-model="showModal"
        width="500"
    >
        <form @submit.prevent="saveSourceBySubsource">
            <v-card>
                <v-card-title>
                    {{ isEdit ? t('relation.edit') : t('relation.new') }}
                </v-card-title>

                <v-card-text>
                    <v-select
                        v-model="form.idMeasurermentSubSource"
                        :items="listData"
                        :label="t('measurementSubsource')"
                        item-title="name"
                        item-value="id"
                        density="compact"
                        variant="outlined"
                        disabled
                    />
                    <v-select
                        v-model="form.idMeasurermentSource"
                        :items="listSource"
                        :label="t('measurementSource')"
                        item-title="name"
                        item-value="id"
                        density="compact"
                        variant="outlined"
                        required
                        clearable
                    />
                    <v-switch
                        v-model="form.isActive"
                        :label="t('state')"
                        true-value="S"
                        false-value="N"
                        color="primary"
                    />
                </v-card-text>

                <v-card-actions>
                    <v-btn
                        color="error"
                        @click="showModal = false"
                    >
                        {{ t('cancel') }}
                    </v-btn>
                    <v-btn
                        color="primary"
                        variant="flat"
                        type="submit"
                    >
                        {{ t('save') }}
                    </v-btn>
                </v-card-actions>
            </v-card>
        </form>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useSubsourceTypes } from '@/composables'
import type { DataTableHeader, SourceBySubsourceRequest } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const {
    loading,
    listData,
    selectedSubsource,
    listSourceData,
    getlistSourceData,
    listSource,
    getListSource,
} = useSubsourceTypes()
const { postInsertOrReplaceSourceBySubsource } = adminService

const showModal = ref(false)
const isEdit = ref(false)

const form = ref<SourceBySubsourceRequest>({
    id: null,
    idMeasurermentSource: null,
    idMeasurermentSubSource: null,
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    {
        text: t('code'),
        key: 'idRelation',
    },
    { text: t('idmeasurementSource'), key: 'id' },
    {
        text: t('description'),
        key: 'name',
    },
    {
        text: t('acronym'),
        key: 'acronym',
    },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
])

const openModal = async (item?: SourceBySubsourceRequest): Promise<void> => {
    if (listSource.value.length === 0) {
        await getListSource()
    }

    if (item) {
        form.value = { ...item }
        isEdit.value = true
    } else {
        form.value = {
            id: null,
            idMeasurermentSource: null,
            idMeasurermentSubSource: selectedSubsource.value?.id,
            isActive: 'S',
        }
        isEdit.value = false
    }
    showModal.value = true
}

const saveSourceBySubsource = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceSourceBySubsource(form.value)
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
        showModal.value = false
        await getlistSourceData()
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}
</script>
