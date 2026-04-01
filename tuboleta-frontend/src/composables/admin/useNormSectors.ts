import type { NormSectors, Resolutions, ResolutionsByNormSectors } from '@/types'
import { ref, computed } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { getNormSectors, getResolutionsByNormSectors, getResolutions } = adminService
const loading = ref<boolean>(false)
const listData = ref<NormSectors[]>([])
const listSourceData = ref<ResolutionsByNormSectors[]>([])
const listResolutions = ref<Resolutions[]>([])
const selectedNormSectors = ref<NormSectors>({
    id: null,
    description: null,
    isActive: null,
})

const showSourceTable = computed(() => !!selectedNormSectors.value?.id)

export const useNormSectors = () => {
    const { t } = useI18n()
    const getListData = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getNormSectors()
            listData.value = data?.list || []
        } catch (err) {
            console.error(t('error.data'), err)
        } finally {
            loading.value = false
        }
    }

    const getListSourceData = async (): Promise<void> => {
        if (!selectedNormSectors.value?.id) {
            listSourceData.value = []
            return
        }

        try {
            loading.value = true
            const { data } = await getResolutionsByNormSectors(selectedNormSectors.value.id)
            listSourceData.value = data?.list || []
        } catch (err) {
            console.error(t('error.data'), err)
        } finally {
            loading.value = false
        }
    }

    const getLisResolutions = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getResolutions(false)
            listResolutions.value = data?.list || []
        } catch (err) {
            console.error(t('error.data'), err)
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        listData,
        getListData,
        selectedNormSectors,
        showSourceTable,
        listSourceData,
        getListSourceData,
        getLisResolutions,
        listResolutions,
    }
}
