package com.tuboleta.backend.repository;

import com.tuboleta.backend.domain.entities.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /** Búsqueda por hash del token (nunca se guarda el token en claro). */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Invalida los tokens previos del usuario al emitir uno nuevo. */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
