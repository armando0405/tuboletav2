import type { IsActive } from '@/types/IsActive'

export interface SourceBySubsourceResponse {
    code: number
    msg: string
    list: SourceBySubsource[]
}

export interface SourceBySubsource {
    id: number
    name: string
    isActive: IsActive
    acronym: string
}
export interface SourceBySubsourceRequest {
    id: number | null
    idMeasurermentSource: number | null
    idMeasurermentSubSource: number | null
    isActive: IsActive | null
}
