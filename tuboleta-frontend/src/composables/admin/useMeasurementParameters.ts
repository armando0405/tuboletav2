import type { NormSectors, ResolutionsByNormSectors, MeasurementParameters } from '@/types'
import { ref, computed } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { getMeasurementParameters } = adminService
const loading = ref<boolean>(false)
const listData = ref<MeasurementParameters[]>([])
const listSourceData = ref<ResolutionsByNormSectors[]>([])
const selectedNormSectors = ref<NormSectors>({
    id: null,
    description: null,
    isActive: null,
})

const showSourceTable = computed(() => !!selectedNormSectors.value?.id)

export const useMeasurementParameters = () => {
    const { t } = useI18n()
    const getListData = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getMeasurementParameters()
            listData.value = data?.list || []
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
    }
}
