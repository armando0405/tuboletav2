import type { IsActive } from '@/types/IsActive'

export interface ResolutionsResponse {
    code: number
    msg: string
    list: Resolutions[]
}

export interface Resolutions {
    id: number | null
    description: string | null
    isActive: IsActive | null
}
