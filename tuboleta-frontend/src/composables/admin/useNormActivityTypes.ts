import type { NormSectors, TypeActivity, TypeActivityByNormSectors } from '@/types'
import { ref, computed } from 'vue'
import { adminService } from '@/utils/services/adminServices'
import { useI18n } from 'vue-i18n'

const { getTypeActivity, getTypeActivityByNormSectors, getNormSectors } = adminService
const loading = ref<boolean>(false)
const listData = ref<TypeActivity[]>([])
const listSourceData = ref<TypeActivityByNormSectors[]>([])
const listNormSectors = ref<NormSectors[]>([])
const selectedTypeActivity = ref<TypeActivity>({
        id: null,
        description: null,
        isActive: null,
})

const showSourceTable = computed(() => !!selectedTypeActivity.value?.id)

export const useTypeActivity = () => {
    const { t } = useI18n()
    const getListData = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getTypeActivity()
            listData.value = data?.list || []
        } catch (err) {
            console.error(t('error.data'), err)
        } finally {
            loading.value = false
        }
    }

    const getListSourceData = async (): Promise<void> => {
        if (!selectedTypeActivity.value?.id) {
            listSourceData.value = []
            return
        }

        try {
            loading.value = true
            const { data } = await getTypeActivityByNormSectors(selectedTypeActivity.value.id)
            listSourceData.value = data?.list || []
        } catch (err) {
            console.error(t('error.data'), err)
        } finally {
            loading.value = false
        }
    }

    const getListNormSectors = async (): Promise<void> => {
        try {
            loading.value = true
            const { data } = await getNormSectors(false)
            listNormSectors.value = data?.list || []
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
        selectedTypeActivity,
        showSourceTable,
        listSourceData,
        getListSourceData,
        getListNormSectors,
        listNormSectors,
    }
}
