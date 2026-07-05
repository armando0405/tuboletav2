package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.SearchNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchNotificationRepository extends JpaRepository<SearchNotification, Long> {
}
