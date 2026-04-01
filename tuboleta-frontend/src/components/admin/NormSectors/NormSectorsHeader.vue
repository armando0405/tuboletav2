<template>
    <v-table-dynamic
        :headers="headers"
        :items="listData"
        :show-create="true"
        :show-edit="true"
        selection-mode="single"
        @selection-change="handleSelectionChange"
        @create="openModal"
        @edit="openModal"
    />
    <v-dialog
        v-model="showModal"
        width="500"
    >
        <form @submit.prevent="saveNormSectors">
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
import { useNormSectors } from '@/composables'
import type { DataTableHeader, NormSectors } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, listData, getListData, selectedNormSectors, getListSourceData, listSourceData } =
    useNormSectors()

const { postInsertOrReplaceNormSectors } = adminService
const showModal = ref<boolean>(false)
const isEdit = ref<boolean>(false)

const form = ref<NormSectors>({
    id: null,
    description: null,
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    { text: t('code'), key: 'id' },
    { text: t('description'), key: 'description' },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
])

const openModal = (item?: NormSectors): void => {
    if (item) {
        isEdit.value = true
        form.value = { ...item }
    } else {
        isEdit.value = false
        form.value = { id: null, description: null, isActive: 'S' }
    }
    showModal.value = true
}

async function handleSelectionChange(row: NormSectors): Promise<void> {
    selectedNormSectors.value = row
    listSourceData.value = []
    await getListSourceData()
}

const saveNormSectors = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceNormSectors(form.value)
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
