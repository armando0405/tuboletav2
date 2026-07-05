package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends JpaRepository<Search, Long> {
}
