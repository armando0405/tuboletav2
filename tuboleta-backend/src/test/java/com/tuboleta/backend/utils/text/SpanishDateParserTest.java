package com.tuboleta.backend.utils.text;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpanishDateParserTest {

    @Test
    void parse_dayAndAbbreviatedMonth_keepsSameYear_whenDateHasNotPassed() {
        LocalDate today = LocalDate.of(2026, 7, 1);
        assertEquals(Optional.of(LocalDate.of(2026, 7, 5)), SpanishDateParser.parse("5 Jul", today));
    }

    @Test
    void parse_dayAndAbbreviatedMonth_addsYear_whenDateAlreadyPassed() {
        LocalDate today = LocalDate.of(2026, 8, 1);
        assertEquals(Optional.of(LocalDate.of(2027, 7, 5)), SpanishDateParser.parse("5 Jul", today));
    }

    @Test
    void parse_fullMonthName() {
        LocalDate today = LocalDate.of(2026, 1, 1);
        assertEquals(Optional.of(LocalDate.of(2026, 4, 11)), SpanishDateParser.parse("11 Abril", today));
    }

    @Test
    void parse_range_usesFirstDate() {
        LocalDate today = LocalDate.of(2026, 1, 1);
        assertEquals(Optional.of(LocalDate.of(2026, 3, 21)), SpanishDateParser.parse("21 Mar - 9 May", today));
    }

    @Test
    void parse_unparseableText_returnsEmpty() {
        assertEquals(Optional.empty(), SpanishDateParser.parse("basura", LocalDate.of(2026, 1, 1)));
    }

    @Test
    void parse_null_returnsEmpty() {
        assertEquals(Optional.empty(), SpanishDateParser.parse(null, LocalDate.of(2026, 1, 1)));
    }
}
