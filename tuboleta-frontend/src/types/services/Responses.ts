export type ObjectListResponse<T = unknown> = {
    code: number
    msg: string
    list?: Array<T>
}

export type ObjectResponse<T = unknown> = {
    code: number
    msg: string
    object?: T
}
