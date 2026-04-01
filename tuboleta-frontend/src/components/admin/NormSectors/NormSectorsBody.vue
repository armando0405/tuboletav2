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
        <form @submit.prevent="saveResolutionsByNormSectors">
            <v-card>
                <v-card-title>
                    {{ isEdit ? t('relation.edit') : t('relation.new') }}
                </v-card-title>

                <v-card-text>
                    <v-select
                        v-model="form.idNormativeSector"
                        :items="listData"
                        :label="t('normSectors')"
                        item-title="description"
                        item-value="id"
                        density="compact"
                        variant="outlined"
                        disabled
                    />

                    <v-select
                        v-model="form.idResolution"
                        :items="listResolutions"
                        :label="t('resolutions')"
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
import { ref, computed } from 'vue'
import { useNormSectors } from '@/composables'
import type { DataTableHeader, ResolutionsByNormSectorsRequest } from '@/types'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { loading, listData, selectedNormSectors, listSourceData, getListSourceData, listResolutions, getLisResolutions } =
    useNormSectors()
const { postInsertOrReplaceResolutionsByNormSectors } = adminService
const showModal = ref<boolean>(false)
const isEdit = ref<boolean>(false)

const form = ref<ResolutionsByNormSectorsRequest>({
    id: null,
    idNormativeSector: null,
    idResolution: null,
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    {
        text: t('code'),
        key: 'id',
    },
    {
        text: t('idResolution'),
        key: 'idResolution',
    },
    {
        text: t('description'),
        key: 'nameResolution',
    },
    {
        text: t('state'),
        key: 'isActive',
        filter: {
            type: 'state',
        },
    },
])

const openModal = async (item?: ResolutionsByNormSectorsRequest): Promise<void> => {
    if (listResolutions.value.length === 0) {
        await getLisResolutions()
    }
    if (item) {
        form.value = { ...item }
        isEdit.value = true
    } else {
        form.value = {
            id: null,
            idNormativeSector: selectedNormSectors.value?.id,
            idResolution: null,
            isActive: 'S',
        }
        isEdit.value = false
    }
    showModal.value = true
}

const saveResolutionsByNormSectors = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceResolutionsByNormSectors(form.value)
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
