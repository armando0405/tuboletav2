package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.ProviderRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRunRepository extends JpaRepository<ProviderRun, Long> {
}
