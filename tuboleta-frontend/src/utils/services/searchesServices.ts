import api from '@/plugins/axios'
import { SEARCHES } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type { Search, SearchCreateRequest, SearchUpdateRequest } from '@/types/services/Search'
import type { Event } from '@/types/services/Event'
import type { EventChange } from '@/types/services/EventChange'
import type { AxiosResponse } from 'axios'

const postSearch = (payload: SearchCreateRequest): Promise<AxiosResponse<ObjectResponse<Search>>> =>
    api.post<ObjectResponse<Search>>(SEARCHES.POST_SEARCH, payload)

const getSearches = (): Promise<AxiosResponse<ObjectListResponse<Search>>> =>
    api.get<ObjectListResponse<Search>>(SEARCHES.GET_SEARCHES)

const getSearchEvents = (id: number): Promise<AxiosResponse<ObjectListResponse<Event>>> =>
    api.get<ObjectListResponse<Event>>(SEARCHES.GET_SEARCH_EVENTS(id))

const getSearchEventChanges = (
    id: number,
    eventId: number,
): Promise<AxiosResponse<ObjectListResponse<EventChange>>> =>
    api.get<ObjectListResponse<EventChange>>(SEARCHES.GET_SEARCH_EVENT_CHANGES(id, eventId))

const patchSearch = (
    id: number,
    payload: SearchUpdateRequest,
): Promise<AxiosResponse<ObjectResponse<Search>>> =>
    api.patch<ObjectResponse<Search>>(SEARCHES.PATCH_SEARCH(id), payload)

const patchToggleSearchProvider = (
    id: number,
    providerId: number,
): Promise<AxiosResponse<ObjectResponse<Search>>> =>
    api.patch<ObjectResponse<Search>>(SEARCHES.PATCH_TOGGLE_SEARCH_PROVIDER(id, providerId))

const patchToggleSearchStatus = (id: number): Promise<AxiosResponse<ObjectResponse<Search>>> =>
    api.patch<ObjectResponse<Search>>(SEARCHES.PATCH_TOGGLE_SEARCH_STATUS(id))

// Devuelve el total de eventos de la búsqueda tras la corrida disparada.
const postRunSearchNow = (id: number): Promise<AxiosResponse<ObjectResponse<number>>> =>
    api.post<ObjectResponse<number>>(SEARCHES.POST_RUN_SEARCH_NOW(id))

const deleteSearch = (id: number): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.delete<ObjectResponse<void>>(SEARCHES.DELETE_SEARCH(id))

export const searchesService = {
    postSearch,
    getSearches,
    getSearchEvents,
    getSearchEventChanges,
    patchSearch,
    patchToggleSearchProvider,
    patchToggleSearchStatus,
    postRunSearchNow,
    deleteSearch,
}
