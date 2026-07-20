import api from '@/plugins/axios'
import { FREQUENCIES } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type { Frequency, FrequencyRequest } from '@/types/services/Frequency'
import type { AxiosResponse } from 'axios'

// Usuario: frecuencias activas para el select de la búsqueda.
const getFrequencies = (): Promise<AxiosResponse<ObjectListResponse<Frequency>>> =>
    api.get<ObjectListResponse<Frequency>>(FREQUENCIES.GET_FREQUENCIES)

// Admin: catálogo completo + CRUD.
const getAdminFrequencies = (): Promise<AxiosResponse<ObjectListResponse<Frequency>>> =>
    api.get<ObjectListResponse<Frequency>>(FREQUENCIES.GET_ADMIN_FREQUENCIES)

const postFrequency = (
    payload: FrequencyRequest,
): Promise<AxiosResponse<ObjectResponse<Frequency>>> =>
    api.post<ObjectResponse<Frequency>>(FREQUENCIES.POST_ADMIN_FREQUENCY, payload)

const putFrequency = (
    id: number,
    payload: FrequencyRequest,
): Promise<AxiosResponse<ObjectResponse<Frequency>>> =>
    api.put<ObjectResponse<Frequency>>(FREQUENCIES.PUT_ADMIN_FREQUENCY(id), payload)

const deleteFrequency = (id: number): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.delete<ObjectResponse<void>>(FREQUENCIES.DELETE_ADMIN_FREQUENCY(id))

export const frequenciesService = {
    getFrequencies,
    getAdminFrequencies,
    postFrequency,
    putFrequency,
    deleteFrequency,
}
