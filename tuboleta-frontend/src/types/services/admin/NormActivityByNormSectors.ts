import type { IsActive } from '@/types/IsActive'

export interface TypeActivityByNormSectorsResponse {
    code: number
    msg: string
    list: TypeActivityByNormSectors[]
}
export interface TypeActivityByNormSectors {
    id: number | null
    idTypeActivity: number | null
    descriptionTypeActivity: string | null
    idNormativeSector: number | null
    descriptionNormativeSector: string | null
    isActive: IsActive | null
}

export interface TypeActivityByNormSectorsRequest {
    id: number | null
    idTypeActivity: number | null
    idNormativeSector: number | null
    isActive: IsActive
}