import type { SubsourceTypes, SourceBySubsource, MeasurementSourceType } from '@/types'
import { ref, computed } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { getSubsourceTypes, getSourceBySubsource, getMeasurementSources } = adminService

const loading = ref<boolean>(false)
const listData = ref<SubsourceTypes[]>([])
const listSourceData = ref<SourceBySubsource[]>([])
const listSource = ref<MeasurementSourceType[]>([])
const selectedSubsource = ref<SubsourceTypes>({
    id: null,
    name: null,
    isActive: null,
})

const showSourceTable = computed(() => !!selectedSubsource.value?.id)

export const useSubsourceTypes = () => {
    const { t } = useI18n()
    const getListData = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getSubsourceTypes()
            listData.value = data?.list || []
        } catch (err) {
            console.error(t('error.save'), err)
        } finally {
            loading.value = false
        }
    }

    const getlistSourceData = async (): Promise<void> => {
        if (!selectedSubsource.value?.id) {
            listSourceData.value = []
            return
        }

        try {
            loading.value = true
            const { data } = await getSourceBySubsource(selectedSubsource.value.id)
            listSourceData.value = data?.list || []
        } catch (err) {
            console.error(t('error.save'), err)
        } finally {
            loading.value = false
        }
    }

    const getListSource = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getMeasurementSources(false)
            listSource.value = data?.list || []
        } catch (err) {
            console.error(t('error.save'), err)
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        listData,
        getListData,
        selectedSubsource,
        listSourceData,
        getlistSourceData,
        showSourceTable,
        getListSource,
        listSource,
    }
}
