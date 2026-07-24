package com.tuboleta.backend.service.scheduler;

import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.repository.SearchProviderRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Selecciona y agrupa el trabajo vencido del scheduler (REQ-DET-004/005):
 * "la BD es la cola". Es puramente síncrona (sin hilos) para poder probarse
 * sin depender del dispatcher.
 */
@Component
public class DueWorkSelector {

    private final SearchProviderRepository searchProviderRepository;

    public DueWorkSelector(SearchProviderRepository searchProviderRepository) {
        this.searchProviderRepository = searchProviderRepository;
    }

    /**
     * Trae los candidatos activos (par activo + búsqueda ACTIVE + usuario
     * ACTIVE + proveedor ACTIVE) con un query de repo, filtra en Java los
     * que están vencidos ({@code lastRunAt == null} o
     * {@code lastRunAt + checkFrequencyHours <= now} — volúmenes pequeños,
     * documentado en REQ-DET-005) y los agrupa por
     * {@code (provider.id, termNormalized)}, ordenando los grupos por el
     * par más vencido primero.
     *
     * @param now instante de referencia para calcular vencimiento
     * @return grupos de trabajo vencido, más vencido primero
     */
    @Transactional(readOnly = true)
    public List<DueGroup> selectDueWork(Instant now) {
        List<SearchProvider> candidates = searchProviderRepository.findActiveCandidatesForScheduling();

        List<SearchProvider> due = candidates.stream()
                .filter(pair -> isDue(pair, now))
                .sorted(Comparator.comparing(pair -> dueSince(pair)))
                .toList();

        Map<String, DueGroup> groups = new LinkedHashMap<>();
        for (SearchProvider pair : due) {
            String key = pair.getProvider().getId() + "::" + pair.getSearch().getTermNormalized();
            groups.computeIfAbsent(key, k -> new DueGroup(
                    pair.getProvider(), pair.getSearch().getTermNormalized(), new ArrayList<>()));
            groups.get(key).pairs().add(pair);
        }
        return new ArrayList<>(groups.values());
    }

    /**
     * Instante en que el par se volvió vencido: {@code Instant.MIN} si nunca
     * corrió (siempre el "más vencido" posible), o
     * {@code lastRunAt + checkFrequencyHours} en otro caso. Ordenar de forma
     * ascendente por este valor deja el más vencido primero.
     */
    private static Instant dueSince(SearchProvider pair) {
        if (pair.getLastRunAt() == null) {
            return Instant.MIN;
        }
        return pair.getLastRunAt().plus(pair.getSearch().getCheckFrequencyMinutes(), ChronoUnit.MINUTES);
    }

    private static boolean isDue(SearchProvider pair, Instant now) {
        return pair.getLastRunAt() == null || !dueSince(pair).isAfter(now);
    }
}
