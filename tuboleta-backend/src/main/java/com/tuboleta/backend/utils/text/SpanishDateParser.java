package com.tuboleta.backend.utils.text;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser best-effort de fechas en español tal como las publican los
 * proveedores (p.ej. TuBoleta): "5 Jul", "11 Abril", o rangos como
 * "21 Mar - 9 May" (se toma la primera fecha).
 *
 * <p>Nunca lanza excepción: si el texto no se puede interpretar, devuelve
 * {@link Optional#empty()}.</p>
 */
public final class SpanishDateParser {

    private static final Pattern DAY_MONTH_PATTERN = Pattern.compile("(\\d{1,2})\\s+([a-z]+)");

    private static final Map<String, Integer> MONTHS_BY_PREFIX = Map.ofEntries(
            Map.entry("ene", 1),
            Map.entry("feb", 2),
            Map.entry("mar", 3),
            Map.entry("abr", 4),
            Map.entry("may", 5),
            Map.entry("jun", 6),
            Map.entry("jul", 7),
            Map.entry("ago", 8),
            Map.entry("sep", 9),
            Map.entry("oct", 10),
            Map.entry("nov", 11),
            Map.entry("dic", 12)
    );

    private SpanishDateParser() {
    }

    /**
     * Igual que {@link #parse(String, LocalDate)} usando {@link LocalDate#now()} como referencia.
     *
     * @param raw texto de fecha a parsear
     * @return la fecha interpretada, o vacío si no se pudo interpretar
     */
    public static Optional<LocalDate> parse(String raw) {
        return parse(raw, LocalDate.now());
    }

    /**
     * Parsea un texto de fecha en español a {@link LocalDate}, best-effort.
     *
     * <p>Soporta día + mes abreviado (3 letras) o nombre completo, con o sin tilde,
     * insensible a mayúsculas. En rangos ("21 Mar - 9 May") toma la primera fecha.
     * Como el texto no trae año, se asume el de {@code today}; si la fecha resultante
     * ya pasó respecto a {@code today}, se asume el año siguiente (los eventos son futuros).</p>
     *
     * @param raw   texto de fecha a parsear
     * @param today fecha de referencia para la heurística de año
     * @return la fecha interpretada, o {@link Optional#empty()} si no se pudo interpretar
     */
    public static Optional<LocalDate> parse(String raw, LocalDate today) {
        if (raw == null || today == null) {
            return Optional.empty();
        }
        String normalized = TermNormalizer.normalize(raw);
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = DAY_MONTH_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return Optional.empty();
        }
        int day = Integer.parseInt(matcher.group(1));
        String monthWord = matcher.group(2);
        if (monthWord.length() < 3) {
            return Optional.empty();
        }
        Integer month = MONTHS_BY_PREFIX.get(monthWord.substring(0, 3));
        if (month == null) {
            return Optional.empty();
        }
        try {
            LocalDate candidate = LocalDate.of(today.getYear(), month, day);
            if (candidate.isBefore(today)) {
                candidate = candidate.plusYears(1);
            }
            return Optional.of(candidate);
        } catch (DateTimeException e) {
            return Optional.empty();
        }
    }
}
