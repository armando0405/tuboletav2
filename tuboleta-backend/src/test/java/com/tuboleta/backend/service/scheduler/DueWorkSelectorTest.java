package com.tuboleta.backend.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.repository.SearchProviderRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos mínimos del brief de Task 6 para la selección/agrupación de trabajo
 * vencido (REQ-DET-004/005). No usa hilos: la selección es síncrona.
 */
@ExtendWith(MockitoExtension.class)
class DueWorkSelectorTest {

    @Mock
    private SearchProviderRepository searchProviderRepository;

    private DueWorkSelector selector;
    private Provider providerA;
    private User user;

    @BeforeEach
    void setUp() {
        selector = new DueWorkSelector(searchProviderRepository);
        providerA = Provider.builder().id(1L).name("TuBoleta").build();
        user = User.builder().id(1L).build();
    }

    private SearchProvider pair(long id, Provider provider, String term, int frequencyHours, Instant lastRunAt) {
        Search search = Search.builder().id(id).user(user).term(term).termNormalized(term)
                .checkFrequencyHours(frequencyHours).build();
        return SearchProvider.builder().id(id).provider(provider).search(search)
                .isActive(true).lastRunAt(lastRunAt).build();
    }

    @Test
    void pairNeverRun_isDue() {
        Instant now = Instant.now();
        SearchProvider pair = pair(1L, providerA, "rock fest", 24, null);
        when(searchProviderRepository.findActiveCandidatesForScheduling()).thenReturn(List.of(pair));

        List<DueGroup> groups = selector.selectDueWork(now);

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).pairs()).containsExactly(pair);
    }

    @Test
    void pairRunFiveHoursAgoWithSixHourFrequency_isNotDue_sevenHoursAgoIsDue() {
        Instant now = Instant.now();
        SearchProvider notDue = pair(1L, providerA, "rock fest", 6, now.minus(5, ChronoUnit.HOURS));
        when(searchProviderRepository.findActiveCandidatesForScheduling()).thenReturn(List.of(notDue));

        assertThat(selector.selectDueWork(now)).isEmpty();

        SearchProvider due = pair(2L, providerA, "rock fest", 6, now.minus(7, ChronoUnit.HOURS));
        when(searchProviderRepository.findActiveCandidatesForScheduling()).thenReturn(List.of(due));

        assertThat(selector.selectDueWork(now)).hasSize(1);
    }

    @Test
    void twoPairsSameProviderAndTerm_formOneGroupWithTwoPairs() {
        Instant now = Instant.now();
        SearchProvider p1 = pair(1L, providerA, "rock fest", 24, null);
        SearchProvider p2 = pair(2L, providerA, "rock fest", 24, null);
        when(searchProviderRepository.findActiveCandidatesForScheduling()).thenReturn(List.of(p1, p2));

        List<DueGroup> groups = selector.selectDueWork(now);

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).pairs()).containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    void differentTerms_formDifferentGroups() {
        Instant now = Instant.now();
        SearchProvider p1 = pair(1L, providerA, "rock fest", 24, null);
        SearchProvider p2 = pair(2L, providerA, "jazz fest", 24, null);
        when(searchProviderRepository.findActiveCandidatesForScheduling()).thenReturn(List.of(p1, p2));

        List<DueGroup> groups = selector.selectDueWork(now);

        assertThat(groups).hasSize(2);
    }

    @Test
    void groupsAreOrderedByMostOverduePairFirst() {
        Instant now = Instant.now();
        // "b term": última corrida hace 10h, frecuencia 6h -> vencido hace 4h (más vencido)
        SearchProvider mostOverdue = pair(1L, providerA, "b term", 6, now.minus(10, ChronoUnit.HOURS));
        // "a term": última corrida hace 7h, frecuencia 6h -> vencido hace 1h (menos vencido)
        SearchProvider lessOverdue = pair(2L, providerA, "a term", 6, now.minus(7, ChronoUnit.HOURS));
        when(searchProviderRepository.findActiveCandidatesForScheduling())
                .thenReturn(List.of(lessOverdue, mostOverdue));

        List<DueGroup> groups = selector.selectDueWork(now);

        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).termNormalized()).isEqualTo("b term");
        assertThat(groups.get(1).termNormalized()).isEqualTo("a term");
    }
}
