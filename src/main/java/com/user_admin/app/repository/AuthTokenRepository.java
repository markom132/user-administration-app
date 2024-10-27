package com.user_admin.app.repository;

import com.user_admin.app.model.AuthToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);

    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}
