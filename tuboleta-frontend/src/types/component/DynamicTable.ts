export type FilterType = 'text' | 'select' | 'date' | 'state'

export type ColumnFilter = {
    type?: FilterType
    items?: unknown[]
}

export type TableHeader = {
    title: string
    key: string
    filterConfig?: boolean | ColumnFilter
    sortable?: boolean
    width?: string | number | undefined
    align?: 'start' | 'center' | 'end'
}

export type DataTableHeader = {
    text?: string
    title?: string | undefined
    value?: string
    key?: string
    filter?: boolean | ColumnFilter
    sortable?: boolean
    width?: string | number | undefined
    align?: 'start' | 'center' | 'end' | undefined
}
