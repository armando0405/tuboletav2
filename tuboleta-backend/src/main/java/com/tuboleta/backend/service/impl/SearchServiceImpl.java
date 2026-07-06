package com.tuboleta.backend.service.impl;

import com.tuboleta.backend.api.dtos.EventResponse;
import com.tuboleta.backend.api.dtos.SearchCreateRequest;
import com.tuboleta.backend.api.dtos.SearchProviderInfo;
import com.tuboleta.backend.api.dtos.SearchResponse;
import com.tuboleta.backend.api.dtos.SearchUpdateRequest;
import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchNotification;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.enums.SearchStatus;
import com.tuboleta.backend.repository.EventRepository;
import com.tuboleta.backend.repository.ProviderRepository;
import com.tuboleta.backend.repository.SearchNotificationRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.repository.SearchRepository;
import com.tuboleta.backend.repository.UserNotificationChannelRepository;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.SearchService;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.exception.GenericException;
import com.tuboleta.backend.utils.exception.NotFoundRegisterException;
import com.tuboleta.backend.utils.text.TermNormalizer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ciclo de vida de una búsqueda (REQ-BUS-001/002/004/005): normalización +
 * unicidad del término, pares con proveedores y fan-out de destinos.
 * Ownership: todo lo ajeno se trata como inexistente (NotFoundRegisterException).
 */
@Service
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;
    private final SearchProviderRepository searchProviderRepository;
    private final SearchNotificationRepository searchNotificationRepository;
    private final ProviderRepository providerRepository;
    private final UserNotificationChannelRepository destinationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public SearchServiceImpl(SearchRepository searchRepository,
                              SearchProviderRepository searchProviderRepository,
                              SearchNotificationRepository searchNotificationRepository,
                              ProviderRepository providerRepository,
                              UserNotificationChannelRepository destinationRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository) {
        this.searchRepository = searchRepository;
        this.searchProviderRepository = searchProviderRepository;
        this.searchNotificationRepository = searchNotificationRepository;
        this.providerRepository = providerRepository;
        this.destinationRepository = destinationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SearchResponse create(Long userId, SearchCreateRequest request) {
        String normalized = TermNormalizer.normalize(request.term());
        searchRepository.findByUserIdAndTermNormalizedAndStatusNot(userId, normalized, SearchStatus.DELETED)
                .ifPresent(existing -> {
                    throw new GenericException(HttpStatus.CONFLICT, ErrorMessage.SEARCH_DUPLICATE, request.term());
                });

        Search search = Search.builder()
                .user(userRepository.getReferenceById(userId))
                .term(request.term())
                .termNormalized(normalized)
                .checkFrequencyHours(request.checkFrequencyHours())
                .status(SearchStatus.ACTIVE)
                .build();
        search = searchRepository.save(search);

        Set<Long> providerIds = new LinkedHashSet<>(request.providerIds());
        for (Long providerId : providerIds) {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new NotFoundRegisterException("Proveedor", "id", providerId));
            SearchProvider pair = SearchProvider.builder()
                    .search(search)
                    .provider(provider)
                    .isActive(true)
                    .build();
            searchProviderRepository.save(pair);
        }

        List<Long> requestedDestinationIds = request.destinationIds() == null ? List.of() : request.destinationIds();
        Set<Long> destinationIds = new LinkedHashSet<>(requestedDestinationIds);
        for (Long destinationId : destinationIds) {
            var destination = destinationRepository.findByIdAndUserId(destinationId, userId)
                    .orElseThrow(() -> new NotFoundRegisterException("Destino", "id", destinationId));
            SearchNotification searchNotification = SearchNotification.builder()
                    .search(search)
                    .userNotificationChannel(destination)
                    .isActive(true)
                    .build();
            searchNotificationRepository.save(searchNotification);
        }

        return toResponse(search);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchResponse> listMine(Long userId) {
        return searchRepository.findByUserIdAndStatusNot(userId, SearchStatus.DELETED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> events(Long userId, Long searchId) {
        ownSearch(userId, searchId);
        List<Long> pairIds = searchProviderRepository.findBySearchId(searchId).stream()
                .map(SearchProvider::getId)
                .toList();
        if (pairIds.isEmpty()) {
            return List.of();
        }
        return eventRepository.findBySearchProviderIdInOrderByLastSeenAtDesc(pairIds).stream()
                .map(SearchServiceImpl::toEventResponse)
                .toList();
    }

    @Override
    @Transactional
    public SearchResponse update(Long userId, Long searchId, SearchUpdateRequest request) {
        Search search = ownSearch(userId, searchId);
        if (request.checkFrequencyHours() != null) {
            search.setCheckFrequencyHours(request.checkFrequencyHours());
        }
        if (request.destinationIds() != null) {
            reconcileDestinations(userId, search, request.destinationIds());
        }
        search = searchRepository.save(search);
        return toResponse(search);
    }

    @Override
    @Transactional
    public SearchResponse togglePair(Long userId, Long searchId, Long providerId) {
        Search search = ownSearch(userId, searchId);
        SearchProvider pair = searchProviderRepository.findBySearchIdAndProviderId(searchId, providerId)
                .orElseThrow(() -> new NotFoundRegisterException("Proveedor de la busqueda", "providerId", providerId));
        pair.setIsActive(!Boolean.TRUE.equals(pair.getIsActive()));
        searchProviderRepository.save(pair);
        return toResponse(search);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long searchId) {
        Search search = ownSearch(userId, searchId);
        search.setStatus(SearchStatus.DELETED);
        searchRepository.save(search);
    }

    private Search ownSearch(Long userId, Long searchId) {
        return searchRepository.findByIdAndUserId(searchId, userId)
                .orElseThrow(() -> new NotFoundRegisterException("Busqueda", "id", searchId));
    }

    private void reconcileDestinations(Long userId, Search search, List<Long> destinationIds) {
        Set<Long> desired = new HashSet<>(destinationIds);
        for (Long destinationId : desired) {
            destinationRepository.findByIdAndUserId(destinationId, userId)
                    .orElseThrow(() -> new NotFoundRegisterException("Destino", "id", destinationId));
        }

        List<SearchNotification> current = searchNotificationRepository.findBySearchId(search.getId());
        Set<Long> alreadyLinked = new HashSet<>();
        for (SearchNotification link : current) {
            Long channelId = link.getUserNotificationChannel().getId();
            alreadyLinked.add(channelId);
            boolean shouldBeActive = desired.contains(channelId);
            if (!Boolean.valueOf(shouldBeActive).equals(link.getIsActive())) {
                link.setIsActive(shouldBeActive);
                searchNotificationRepository.save(link);
            }
        }
        for (Long destinationId : desired) {
            if (alreadyLinked.contains(destinationId)) {
                continue;
            }
            var destination = destinationRepository.findByIdAndUserId(destinationId, userId).orElseThrow();
            SearchNotification link = SearchNotification.builder()
                    .search(search)
                    .userNotificationChannel(destination)
                    .isActive(true)
                    .build();
            searchNotificationRepository.save(link);
        }
    }

    private SearchResponse toResponse(Search search) {
        List<SearchProviderInfo> providers = searchProviderRepository.findBySearchId(search.getId()).stream()
                .map(pair -> new SearchProviderInfo(
                        pair.getProvider().getId(),
                        pair.getProvider().getName(),
                        pair.getIsActive(),
                        pair.getProvider().getStatus(),
                        pair.getLastRunAt()))
                .toList();
        return new SearchResponse(search.getId(), search.getTerm(), search.getCheckFrequencyHours(),
                search.getStatus(), providers);
    }

    private static EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getSearchProvider().getProvider().getName(),
                event.getExternalId(),
                event.getTitle(),
                event.getVenue(),
                event.getEventDateRaw(),
                event.getEventDate(),
                event.getStatus(),
                event.getMissCount(),
                event.getFirstSeenAt(),
                event.getLastSeenAt());
    }
}
