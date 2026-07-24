import api from '@/plugins/axios'
import { ADMIN_PROVIDERS } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type { ProviderAdmin, ProviderDisableRequest } from '@/types/services/ProviderAdmin'
import type { AxiosResponse } from 'axios'

const getProviders = (): Promise<AxiosResponse<ObjectListResponse<ProviderAdmin>>> =>
    api.get<ObjectListResponse<ProviderAdmin>>(ADMIN_PROVIDERS.GET_ADMIN_PROVIDERS)

const postDisableProvider = (
    id: number,
    payload: ProviderDisableRequest,
): Promise<AxiosResponse<ObjectResponse<ProviderAdmin>>> =>
    api.post<ObjectResponse<ProviderAdmin>>(ADMIN_PROVIDERS.POST_DISABLE_PROVIDER(id), payload)

const postEnableProvider = (id: number): Promise<AxiosResponse<ObjectResponse<ProviderAdmin>>> =>
    api.post<ObjectResponse<ProviderAdmin>>(ADMIN_PROVIDERS.POST_ENABLE_PROVIDER(id))

export const adminProvidersService = {
    getProviders,
    postDisableProvider,
    postEnableProvider,
}
