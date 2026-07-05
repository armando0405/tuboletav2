package com.tuboleta.backend.utils.text;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalización de términos de búsqueda (REQ-BUS-002) y coincidencia
 * "contiene la frase" contra resultados de proveedores (REQ-BUS-003).
 *
 * <p>La normalización aplica trim, minúsculas, colapso de espacios múltiples
 * y eliminación de tildes/diacríticos. La eliminación de diacríticos convierte
 * la ñ en n; es una decisión consciente para tolerancia de matching.</p>
 */
public final class TermNormalizer {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    private TermNormalizer() {
    }

    /**
     * Normaliza un término: trim, minúsculas, colapso de espacios y
     * eliminación de tildes/diacríticos.
     *
     * @param input texto a normalizar
     * @return texto normalizado, o cadena vacía si {@code input} es {@code null} o blanco
     */
    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String trimmedLower = input.trim().toLowerCase(Locale.ROOT);
        String collapsed = MULTIPLE_SPACES.matcher(trimmedLower).replaceAll(" ");
        String decomposed = Normalizer.normalize(collapsed, Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposed).replaceAll("");
    }

    /**
     * Determina si {@code candidateText}, una vez normalizado, contiene
     * {@code normalizedTerm} como subcadena (REQ-BUS-003).
     *
     * @param candidateText texto candidato (sin normalizar), p.ej. el título de un resultado
     * @param normalizedTerm término ya normalizado contra el que se compara
     * @return {@code true} si el candidato normalizado contiene el término; {@code false} si el término es blanco
     */
    public static boolean matches(String candidateText, String normalizedTerm) {
        if (normalizedTerm == null || normalizedTerm.isBlank()) {
            return false;
        }
        return normalize(candidateText).contains(normalizedTerm);
    }
}
