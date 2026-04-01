<template>
    <v-table-dynamic
        :headers="headers"
        :items="listData"
        :show-create="true"
        :show-edit="true"
        :show-delete="true"
        selection-mode="single"
        @create="openModal"
        @edit="openModal"
        @delete="handleDelete"
    />

    <v-dialog
        v-model="showModal"
        width="500"
    >
        <form @submit.prevent="saveMeasurementParameters">
            <v-card>
                <v-card-title>
                    {{ isEdit ? t('relation.edit') : t('relation.new') }}
                </v-card-title>

                <v-card-text>
                    <v-text-field
                        v-model="form.description"
                        :label="t('description')"
                        required
                    />

                    <v-switch
                        v-model="form.isActive"
                        :label="t('state')"
                    />
                </v-card-text>

                <v-card-actions>
                    <v-btn
                        color="error"
                        @click="showModal = false"
                    >
                        {{ t('cancel') }}
                    </v-btn>
                    <v-btn type="submit">
                        {{ t('save') }}
                    </v-btn>
                </v-card-actions>
            </v-card>
        </form>
    </v-dialog>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMeasurementParameters } from '@/composables/admin/useMeasurementParameters'
import { adminService } from '@/utils/services/adminServices'
import type { DataTableHeader, MeasurementParameters, MeasurementParametersRequest } from '@/types'

const { getListData, listData, loading } = useMeasurementParameters()
const { postInsertOrReplaceMeasurementParameters } = adminService
const { t } = useI18n()
const showModal = ref(false)
const isEdit = ref(false)
const form = ref<MeasurementParametersRequest>({
    id: null,
    code: null,
    description: null,
    measurementUnit: null,
    requiredInSelfDeclaration: null,
    appliesTariff: null,
    liquidationConcept: null,
    waterUse: null,
    requiresLaboratory: null,
    isActive: 'S',
    homologationCode: null,
})

const headers = computed<DataTableHeader[]>(() => [
    { title: t('code'), value: 'code' },
    { title: t('description'), value: 'description' },
    { title: t('measurementUnit'), value: 'measurementUnit' },
    {
        title: t('requiredInSelfDeclaration'),
        value: 'requiredInSelfDeclaration',
        filter: {
            type: 'state',
        },
    },
    {
        title: t('appliesTariff'),
        value: 'appliesTariff',
        filter: {
            type: 'state',
        },
    },
    { title: t('liquidationConcept'), value: 'liquidationConcept' },
    { title: t('waterUse'), value: 'waterUse' },
    {
        title: t('requiresLaboratory'),
        value: 'requiresLaboratory',
        filter: {
            type: 'state',
        },
    },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
    { title: t('homologationCode'), value: 'homologationCode' },
])

const openModal = (item?: MeasurementParameters): void => {
    if (item) {
        isEdit.value = true
        form.value = { ...item }
    } else {
        isEdit.value = false
        form.value = {
            id: null,
            code: null,
            description: null,
            measurementUnit: null,
            requiredInSelfDeclaration: 'N',
            appliesTariff: 'N',
            liquidationConcept: null,
            waterUse: null,
            requiresLaboratory: 'N',
            isActive: 'S',
            homologationCode: null,
        }
    }
    showModal.value = true
}

// async function handleSelectionChange(row: MeasurementParameters): Promise<void> {
//     selectedNormSectors.value = row
//     listSourceData.value = []
//     await getListSourceData()
// }

const handleDelete = async (item: MeasurementParameters): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceMeasurementParameters({ ...item, isActive: 'N' })
        await getListData()
        window.swal.fire(t('success.title'), t('success.deleted'), 'success')
    } catch (error) {
        console.error(t('error.delete'), error)
    } finally {
        loading.value = false
    }
}
const saveMeasurementParameters = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceMeasurementParameters(form.value)
        await getListData()
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
        showModal.value = false
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}
onMounted(async () => {
    await getListData()
})
</script>
