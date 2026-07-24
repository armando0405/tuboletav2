import api from '@/plugins/axios'
import { ADMIN_PROVIDERS } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type {
    ProviderAdmin,
    ProviderDisableRequest,
    ProviderSaveRequest,
} from '@/types/services/ProviderAdmin'
import type { AxiosResponse } from 'axios'

const getProviders = (): Promise<AxiosResponse<ObjectListResponse<ProviderAdmin>>> =>
    api.get<ObjectListResponse<ProviderAdmin>>(ADMIN_PROVIDERS.GET_ADMIN_PROVIDERS)

const postProvider = (
    payload: ProviderSaveRequest,
): Promise<AxiosResponse<ObjectResponse<ProviderAdmin>>> =>
    api.post<ObjectResponse<ProviderAdmin>>(ADMIN_PROVIDERS.POST_PROVIDER, payload)

const patchProvider = (
    id: number,
    payload: ProviderSaveRequest,
): Promise<AxiosResponse<ObjectResponse<ProviderAdmin>>> =>
    api.patch<ObjectResponse<ProviderAdmin>>(ADMIN_PROVIDERS.PATCH_PROVIDER(id), payload)

const postDisableProvider = (
    id: number,
    payload: ProviderDisableRequest,
): Promise<AxiosResponse<ObjectResponse<ProviderAdmin>>> =>
    api.post<ObjectResponse<ProviderAdmin>>(ADMIN_PROVIDERS.POST_DISABLE_PROVIDER(id), payload)

const postEnableProvider = (id: number): Promise<AxiosResponse<ObjectResponse<ProviderAdmin>>> =>
    api.post<ObjectResponse<ProviderAdmin>>(ADMIN_PROVIDERS.POST_ENABLE_PROVIDER(id))

export const adminProvidersService = {
    getProviders,
    postProvider,
    patchProvider,
    postDisableProvider,
    postEnableProvider,
}
