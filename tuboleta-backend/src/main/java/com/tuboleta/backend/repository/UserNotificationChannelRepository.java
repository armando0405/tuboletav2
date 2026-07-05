package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.UserNotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationChannelRepository extends JpaRepository<UserNotificationChannel, Long> {
}
