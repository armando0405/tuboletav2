import type { ResolutionBySubSources, Resolutions, SubsourceTypes } from '@/types'
import { ref, computed } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { getResolutions, getSubsourceTypes, getResolutionBySubSource } = adminService

const loading = ref<boolean>(true)
const listData = ref<Resolutions[]>([])
const listSubsourceTypes = ref<SubsourceTypes[]>([])
const listDataBySubSource = ref<ResolutionBySubSources[]>([])

const selectedResolution = ref<Resolutions>({
    id: null,
    description: null,
    isActive: null,
})
const showSubsourceCard = computed(() => !!selectedResolution.value?.id)

export const useResolutions = () => {
    const { t } = useI18n()
    const getListData = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getResolutions()
            listData.value = data?.list || []
        } catch (err) {
            console.error(t('error.save'), err)
        } finally {
            loading.value = false
        }
    }

    const getListSubsourceTypes = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getSubsourceTypes(false)
            listSubsourceTypes.value = data?.list || []
        } catch (err) {
            console.error(t('error.save'), err)
        } finally {
            loading.value = false
        }
    }

    const getListDataBySubSource = async (): Promise<void> => {
        if (!selectedResolution.value?.id) {
            listDataBySubSource.value = []
            return
        }
        try {
            loading.value = true
            const { data } = await getResolutionBySubSource(selectedResolution.value?.id)
            listDataBySubSource.value = data?.list || []
        } catch (err) {
            console.error(t('error.save'), err)
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        listData,
        listSubsourceTypes,
        getListData,
        selectedResolution,
        getListSubsourceTypes,
        getListDataBySubSource,
        listDataBySubSource,
        showSubsourceCard,
    }
}
