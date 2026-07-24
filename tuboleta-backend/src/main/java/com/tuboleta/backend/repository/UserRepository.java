package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Login por email (REQ-USU-002) y verificación de duplicados en el
     * registro.
     */
    Optional<User> findByEmail(String email);
}
