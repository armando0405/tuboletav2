import type { IsActive } from '@/types/IsActive'
export interface SubsourceTypesResponse {
    code: number
    msg: string
    list: SubsourceTypes[]
}
export interface SubsourceTypes {
    id: number | null
    name: string | null
    isActive: IsActive | null
}
