import type { IsActive } from '@/types/IsActive'

export interface MeasurementParameters {
    id: number | null
    code: string | null
    description: string | null
    isActive: IsActive | string | null
    measurementUnit: string | null
    requiredInSelfDeclaration: string | null
    appliesTariff: string | null
    liquidationConcept: string | null
    waterUse: string | null
    requiresLaboratory: string | null
    homologationCode: string | null
}
export interface MeasurementParametersRequest {
    id: number | null
    code: string | null
    description: string | null
    isActive: IsActive | string | null
    measurementUnit: string | null
    requiredInSelfDeclaration: string | null
    appliesTariff: string | null
    liquidationConcept: string | null
    waterUse: string | null
    requiresLaboratory: string | null
    homologationCode: string | null
}
