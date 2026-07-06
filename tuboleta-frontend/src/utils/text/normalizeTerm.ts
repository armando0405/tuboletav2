// Espejo COSMÉTICO, solo para el preview en el formulario de creación
// (REQ-FE-001 / diseno-frontend.md), de TermNormalizer.normalize del backend
// (REQ-BUS-002): trim, minúsculas, colapso de espacios múltiples y
// eliminación de tildes/diacríticos. El backend sigue siendo la única fuente
// de verdad para la normalización real y la validación de duplicados (409).

// Bloque Unicode "Combining Diacritical Marks" (U+0300..U+036F) que queda
// tras normalizar en forma NFD (p.ej. "e" + acento agudo combinante).
const COMBINING_MARKS_START = 0x0300
const COMBINING_MARKS_END = 0x036f

function stripCombiningMarks(text: string): string {
    let result = ''
    for (const char of text) {
        const code = char.codePointAt(0) ?? 0
        if (code < COMBINING_MARKS_START || code > COMBINING_MARKS_END) {
            result += char
        }
    }
    return result
}

export function previewNormalizedTerm(input: string): string {
    if (!input || !input.trim()) return ''

    const trimmedLower = input.trim().toLowerCase()
    const collapsed = trimmedLower.replace(/\s+/g, ' ')

    return stripCombiningMarks(collapsed.normalize('NFD'))
}
