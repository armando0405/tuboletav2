<template>
    <div>
        <v-table-dynamic
            :headers="headers"
            :items="listDataBySubSource"
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
        <form @submit.prevent="saveResolutions">
            <v-card>
                <v-card-title>
                    {{ isEdit ? t('relation.edit') : t('relation.new') }}
                </v-card-title>

                <v-card-text>
                    <v-text-field
                        v-model="selectedResolution.description"
                        :items="Resolutions"
                        :label="t('resolutions')"
                        density="compact"
                        variant="outlined"
                        disabled
                    ></v-text-field>

                    <v-select
                        v-model="form.idMeasurermentSubSource"
                        :items="listSubsourceTypes"
                        :label="t('measurementSubsource')"
                        item-title="name"
                        item-value="id"
                        density="compact"
                        variant="outlined"
                        required
                        clearable
                    ></v-select>

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
                        {{t('save')}}
                    </v-btn>
                </v-card-actions>
            </v-card>
        </form>
    </v-dialog>
</template>

<script setup lang="ts">
import { useResolutions } from '@/composables'
import type {
    DataTableHeader,
    ResolutionBySubSources,
    ResolutionBySubSourcesRequest,
} from '@/types'
import { adminService } from '@/utils/services/adminServices'
import Resolutions from '@/views/admin/Resolutions.vue'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const {
    loading,
    selectedResolution,
    listSubsourceTypes,
    listDataBySubSource,
    getListDataBySubSource,
    getListSubsourceTypes,
} = useResolutions()

const { postInsertOrReplaceResolutionBySubSources } = adminService

const showModal = ref<boolean>(false)
const isEdit = ref<boolean>(false)

const form = ref<ResolutionBySubSourcesRequest>({
    id: null,
    idResolution: null,
    idMeasurermentSubSource: null,
    isActive: 'S',
})

const headers = computed<DataTableHeader[]>(() => [
    {
        text: t('code'),
        key: 'idRelation',
    },
    {
        text: t('idSubsource'),
        key: 'id',
    },
    {
        text: t('description'),
        key: 'name',
    },
    {
        text: t('state'),
        key: 'isActiveRelation',
        filter: {
            type: 'state',
        },
    },
])

const openModal = async (item?: ResolutionBySubSourcesRequest): Promise<void> => {
    
    if (listSubsourceTypes.value.length === 0) {
        await getListSubsourceTypes()
    }
    if (item) {
        form.value = { ...item }
        isEdit.value = true
    } else {
        form.value = {
            id: null,
            idResolution: selectedResolution.value?.id,
            idMeasurermentSubSource: null,
            isActive: 'S',
        }
        isEdit.value = false
    }
    showModal.value = true
}

const saveResolutions = async (): Promise<void> => {
    try {
        loading.value = true
        await postInsertOrReplaceResolutionBySubSources(form.value)
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
        showModal.value = false
        await getListDataBySubSource()
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}
</script>
