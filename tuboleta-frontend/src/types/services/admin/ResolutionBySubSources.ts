import type { IsActive } from '@/types/IsActive'

export interface ResolutionBySubSourcesResponse {
    code: number
    msg: string
    list: ResolutionBySubSources[]
}
export interface ResolutionBySubSources {
    id: number
    idRelation: number
    name: string
    idPliga: null
    isActive: IsActive
    isActiveRelation: IsActive
}

export interface ResolutionBySubSourcesRequest {
    id: number | null
    idResolution: number | null
    idMeasurermentSubSource: number | null
    isActive: IsActive
}
