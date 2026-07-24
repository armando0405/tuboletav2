package com.tuboleta.backend.utils.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TermNormalizerTest {

    @Test
    void normalize_trimsLowercasesAndCollapsesSpaces() {
        assertEquals("fucks news", TermNormalizer.normalize("  Fucks News  "));
    }

    @Test
    void normalize_removesAccents() {
        assertEquals("cafe tacvba", TermNormalizer.normalize("Café  Tacvba"));
    }

    @Test
    void normalize_null_returnsEmptyString() {
        assertEquals("", TermNormalizer.normalize(null));
    }

    @Test
    void normalize_blank_returnsEmptyString() {
        assertEquals("", TermNormalizer.normalize("   "));
    }

    @Test
    void matches_whenCandidateContainsNormalizedTerm_returnsTrue() {
        assertTrue(TermNormalizer.matches("FUCKS NEWS EN BOGOTÁ", "fucks news"));
    }

    @Test
    void matches_whenCandidateDoesNotContainTerm_returnsFalse() {
        assertFalse(TermNormalizer.matches("Festival de noticias rock", "fucks news"));
    }

    @Test
    void matches_whenNormalizedTermIsBlank_returnsFalse() {
        assertFalse(TermNormalizer.matches("cualquier texto", "   "));
    }
}
