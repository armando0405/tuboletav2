import api from '@/plugins/axios'
import { PROVIDERS } from '@/utils/endpoints'
import type { ObjectListResponse } from '@/types/services/Responses'
import type { Provider } from '@/types/services/Provider'
import type { AxiosResponse } from 'axios'

const getProviders = (): Promise<AxiosResponse<ObjectListResponse<Provider>>> =>
    api.get<ObjectListResponse<Provider>>(PROVIDERS.GET_PROVIDERS)

export const providersService = {
    getProviders,
}
