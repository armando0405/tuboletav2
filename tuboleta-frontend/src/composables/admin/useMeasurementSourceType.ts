import type { MeasurementSourceType } from '@/types'
import { ref } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { getMeasurementSources } = adminService

const loading = ref<boolean>(true)
const listData = ref<MeasurementSourceType[]>([])

export const useMeasurementSourceType = () => {
    const { t } = useI18n()
    const getListData = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getMeasurementSources()
            listData.value = data?.list || []
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
    }
}
