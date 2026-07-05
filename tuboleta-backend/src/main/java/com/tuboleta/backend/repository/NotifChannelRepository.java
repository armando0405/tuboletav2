package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.NotifChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifChannelRepository extends JpaRepository<NotifChannel, Long> {
}
