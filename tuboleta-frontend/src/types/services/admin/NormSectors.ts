import type { IsActive } from '@/types/IsActive'
export interface NormSectorsResponse {
    code: number
    msg: string
    list: NormSectors[]
}
export interface NormSectors {
    id: number | null
    description: string | null
    isActive: IsActive | null
}