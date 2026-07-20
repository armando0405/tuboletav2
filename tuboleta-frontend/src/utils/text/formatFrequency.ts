// Formatea una frecuencia en minutos a un texto humano en español:
// "cada 10 min", "cada 6 h", "cada día", "cada 2 días".
export function formatFrequency(minutes: number | null | undefined): string {
    if (minutes == null) return ''
    if (minutes < 60) return `cada ${minutes} min`
    if (minutes < 1440) {
        const hours = minutes / 60
        return Number.isInteger(hours) ? `cada ${hours} h` : `cada ${minutes} min`
    }
    const days = minutes / 1440
    if (days === 1) return 'cada día'
    return Number.isInteger(days) ? `cada ${days} días` : `cada ${minutes} min`
}
