package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.NotifChannel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifChannelRepository extends JpaRepository<NotifChannel, Long> {

    Optional<NotifChannel> findByName(String name);
}
