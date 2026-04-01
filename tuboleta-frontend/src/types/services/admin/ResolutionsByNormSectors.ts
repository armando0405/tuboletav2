import type { IsActive } from '@/types/IsActive'

export interface ResolutionsByNormSectorsResponse {
    code: number
    msg: string
    list: ResolutionsByNormSectors[]
}
export interface ResolutionsByNormSectors {
    id: number | null
    idNormativeSector: number | null
    nameNormativeSector: string | null
    idResolution: number | null
    nameResolution: string | null
    isActive: IsActive | null
}
export interface ResolutionsByNormSectorsRequest {
    id: number | null
    idNormativeSector: number | null
    idResolution: number | null
    isActive: IsActive
}