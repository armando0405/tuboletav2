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
        <form @submit.prevent="saveTypeActivityByNormsSectors">
            <v-card>
                <v-card-title>
                    {{ isEdit ? t('relation.edit') : t('relation.new') }}
                </v-card-title>

                <v-card-text>
                    <v-select
                        v-model="form.idTypeActivity"
                        :items="listData"
                        :label="t('activityType')"
                        item-title="description"
                        item-value="id"
                        density="compact"
                        variant="outlined"
                        disabled
                    />

                    <v-select
                        v-model="form.idNormativeSector"
                        :items="listNormSectors"
                        :label="t('normSectors')"
                        item-title="description"
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
                        variant="flat"
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
import { useTypeActivity } from '@/composables/admin/useNormActivityTypes'
import type { DataTableHeader, TypeActivityByNormSectorsRequest } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { loading, listData, selectedTypeActivity, listSourceData, getListSourceData, getListNormSectors, listNormSectors } =
    useTypeActivity()
const { postInsertOrReplaceTypeActivityByNormSectors } = adminService
const showModal = ref<boolean>(false)
const isEdit = ref<boolean>(false)

const form = ref<TypeActivityByNormSectorsRequest>({
    id: null,
    idTypeActivity: null,
    idNormativeSector: null,
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    {
        text: t('code'),
        key: 'id',
    },
    {
        text: t('idNormSectors'),
        key: 'idNormativeSector',
    },
    {
        text: t('description'),
        key: 'descriptionNormativeSector',
    },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
])

const openModal = async (item?: TypeActivityByNormSectorsRequest): Promise<void> => {
    if (listNormSectors.value.length === 0) {
        await getListNormSectors()
    }
    if (item) {
        form.value = { ...item }
        isEdit.value = true
    } else {
        form.value = {
            id: null,
            idTypeActivity: selectedTypeActivity.value?.id,
            idNormativeSector: null,
            isActive: 'S',
        }
        isEdit.value = false
    }
    showModal.value = true
}

const saveTypeActivityByNormsSectors = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceTypeActivityByNormSectors(form.value)
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
        showModal.value = false
        await getListSourceData()
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}
</script>
