import api from '@/plugins/axios'
import { DESTINATIONS } from '@/utils/endpoints'
import type { ObjectListResponse, ObjectResponse } from '@/types/services/Responses'
import type { Destination, DestinationRequest } from '@/types/services/Destination'
import type { AxiosResponse } from 'axios'

const getDestinations = (): Promise<AxiosResponse<ObjectListResponse<Destination>>> =>
    api.get<ObjectListResponse<Destination>>(DESTINATIONS.GET_DESTINATIONS)

const postDestination = (
    payload: DestinationRequest,
): Promise<AxiosResponse<ObjectResponse<Destination>>> =>
    api.post<ObjectResponse<Destination>>(DESTINATIONS.POST_DESTINATION, payload)

const patchToggleDestination = (id: number): Promise<AxiosResponse<ObjectResponse<Destination>>> =>
    api.patch<ObjectResponse<Destination>>(DESTINATIONS.PATCH_TOGGLE_DESTINATION(id))

export const destinationsService = {
    getDestinations,
    postDestination,
    patchToggleDestination,
}
