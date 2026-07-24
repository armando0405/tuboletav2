// Refleja FrequencyResponse: catálogo de frecuencias de monitoreo (en minutos).
export type Frequency = {
    id: number
    label: string
    minutes: number
    isActive: boolean
}

// Refleja FrequencyRequest (alta/edición desde el panel admin).
export type FrequencyRequest = {
    label: string
    minutes: number
    isActive?: boolean
}
