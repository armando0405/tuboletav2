<template>
    <div>
        <v-table-dynamic
            :headers="headers"
            :items="listData"
            selection-mode="single"
            :show-create="true"
            :show-edit="true"
            @create="openModal"
            @edit="openModal"
        />
    </div>

    <v-dialog
        v-model="showModal"
        width="500"
    >
        <v-card>
            <v-card-title>
                {{ isEdit ? t('type.edit') : t('type.new') }}
            </v-card-title>

            <v-card-text>
                <v-text-field
                    v-model="form.name"
                    :label="t('description')"
                    density="compact"
                    variant="outlined"
                />

                <v-text-field
                    v-model="form.acronym"
                    :label="t('acronym')"
                    density="compact"
                    variant="outlined"
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
                    @click="saveMeasurementSources"
                >
                    {{ t('save') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { useMeasurementSourceType } from '@/composables'
import type { DataTableHeader, MeasurementSourceType } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, listData, getListData } = useMeasurementSourceType()
const { postInsertOrReplaceMeasurementSources } = adminService

const isEdit = ref<boolean>(false)
const showModal = ref<boolean>(false)

const form = ref<MeasurementSourceType>({
    id: -1,
    name: '',
    acronym: '',
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    {
        text: t('code'),
        key: 'id',
    },
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

const openModal = (item?: MeasurementSourceType): void => {
    if (item) {
        isEdit.value = true
        form.value = {
            ...item,
        }
    } else {
        isEdit.value = false
        form.value = {
            id: -1,
            name: '',
            acronym: '',
            isActive: 'S',
        }
    }

    showModal.value = true
}

const saveMeasurementSources = async (): Promise<void> => {
    try {
        loading.value = true

        const { data } = await postInsertOrReplaceMeasurementSources(form.value)
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
        if (data?.code === 0) {
            await getListData()
            showModal.value = false
        }
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}
</script>
