package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.EventChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventChangeRepository extends JpaRepository<EventChange, Long> {
}
