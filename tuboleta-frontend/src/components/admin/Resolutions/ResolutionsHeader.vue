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
            @selection-change="handleSelectionChange"
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
                    v-model="form.description"
                    :label="t('description')"
                    density="compact"
                    variant="outlined"
                    required
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
                    @click="saveResolutions"
                >
                    {{t('save')}}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { useResolutions } from '@/composables'
import type { DataTableHeader, Resolutions } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const {
    loading,
    listData,
    getListData,
    selectedResolution,
    getListDataBySubSource,
    listDataBySubSource,
} = useResolutions()
const { postInsertOrReplaceResolutions } = adminService
const showModal = ref(false)
const isEdit = ref(false)

const form = ref<Resolutions>({
    id: 0,
    description: '',
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    {
        text: t('code'),
        key: 'id',
    },
    {
        text: t('description'),
        key: 'description',
    },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
])

const openModal = (item?: Resolutions): void => {
    if (item) {
        form.value = { ...item }
        isEdit.value = true
    } else {
        form.value = {
            id: -1,
            description: '',
            isActive: 'S',
        }
        isEdit.value = false
    }
    showModal.value = true
}

async function handleSelectionChange(row: Resolutions): Promise<void> {
    selectedResolution.value = row
    listDataBySubSource.value = []
    await getListDataBySubSource()
}

const saveResolutions = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceResolutions(form.value)
        await getListData()
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
        showModal.value = false
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}
</script>
