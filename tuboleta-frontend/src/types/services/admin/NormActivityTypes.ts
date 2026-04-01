import type { IsActive } from '@/types/IsActive'
export interface TypeActivityResponse {
    code: number
    msg: string
    list: TypeActivity[]
}
export interface TypeActivity {
    id: number | null
    description: string | null
    isActive: IsActive | null
}