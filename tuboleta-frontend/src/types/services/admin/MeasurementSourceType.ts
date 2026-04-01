import type { IsActive } from '@/types/IsActive'
export interface MeasurementSourceTypeResponse {
    code: number
    msg: string
    list: MeasurementSourceType[]
}
export interface MeasurementSourceType {
    id: number | null
    name: string | null
    isActive: IsActive | null
    acronym: string | null
}
