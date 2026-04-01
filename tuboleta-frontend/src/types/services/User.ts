export type User = {
    id: number | string
    userId: number
    name: string
    username: string
    email: string
    role?: string
    dependencyId: number
    active: string
    positionId: number
    idCard: number
    companyId: number
    domain: string | null
    usesDomain: string | null
    image: string | null
    cellphone: number
    dynamicKeyActive: string | null
    dynamicKey: string | null
    validityDate: string | null
    dynamicKeyDate: string
}
