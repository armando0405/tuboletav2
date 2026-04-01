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
        <form @submit.prevent="saveSubsourceTypes">
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
                        {{t('cancel')}}
                    </v-btn>
                    <v-btn
                        color="primary"
                        variant="flat"
                        type="submit"
                    >
                       {{t('save')}}
                    </v-btn>
                </v-card-actions>
            </v-card>
        </form>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useSubsourceTypes } from '@/composables'
import type { DataTableHeader, SubsourceTypes } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { loading, listData, getListData, selectedSubsource, getlistSourceData, listSourceData } =
    useSubsourceTypes()
const { postInsertOrReplaceSubsourceTypes } = adminService
const showModal = ref(false)
const isEdit = ref(false)

const form = ref<SubsourceTypes>({
    id: null,
    name: null,
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    { text: t('code'), key: 'id' },
    { text: t('description'), key: 'name' },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
])

const openModal = (item?: SubsourceTypes): void => {
    if (item) {
        isEdit.value = true
        form.value = { ...item }
    } else {
        isEdit.value = false
        form.value = { id: null, name: null, isActive: null }
    }
    showModal.value = true
}

async function handleSelectionChange(row: SubsourceTypes): Promise<void> {
    selectedSubsource.value = row
    listSourceData.value = []
    await getlistSourceData()

}

const saveSubsourceTypes = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceSubsourceTypes(form.value)
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
