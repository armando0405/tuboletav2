<template>
    <v-floating-card :title="t('laboratories')">
        <v-table-dynamic
            :headers="headers"
            :items="listData"
            selection-mode="single"
        >
            <template #toolbar-actions>
                <v-tooltip>
                    <template #activator="{ props }">
                        <v-btn
                            v-bind="props"
                            color="success"
                            prepend-icon="mdi-refresh"
                            @click="handleSyncLaboratories"
                        >
                            {{t('synchronize')}}
                        </v-btn>
                    </template>
                </v-tooltip>
            </template>
        </v-table-dynamic>
    </v-floating-card>
    <v-loading :active="loading" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import type { Laboratories } from '@/types'
import type { DataTableHeader } from '@/types/component/DynamicTable'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { getLaboratories, postSyncLaboratories } = adminService
const loading = ref<boolean>(false)

const listData = ref<Laboratories[]>([])

const headers = computed<DataTableHeader[]>(() => [
    { text: t('code'), key: 'code' },
    { text: t('name'), key: 'name' },
    { text: t('nit'), key: 'nit' },
    { text: t('city'), key: 'city' },
    { text: t('address'), key: 'address' },
    { text: t('phone'), key: 'phone' },
    { text: t('contact'), key: 'contact' },
    { text: t('email'), key: 'email' },
])

const getListData = async (): Promise<void> => {
    try {
        loading.value = true
        const { data } = await getLaboratories()
        listData.value = data?.list ?? []
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}

const handleSyncLaboratories = async (): Promise<void> => {
    try {
        loading.value = true
        await postSyncLaboratories()
        await getListData()
        window.swal.fire(t('success.title'), t('success.saved'), 'success')
    } catch (error) {
        console.error(t('error.save'), error)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    getListData()
})
</script>
