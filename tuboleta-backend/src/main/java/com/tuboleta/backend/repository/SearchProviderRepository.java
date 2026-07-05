package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.SearchProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchProviderRepository extends JpaRepository<SearchProvider, Long> {
}
