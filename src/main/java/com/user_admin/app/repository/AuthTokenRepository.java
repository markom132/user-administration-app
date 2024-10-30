package com.user_admin.app.repository;

import com.user_admin.app.model.AuthToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for managing {@link AuthToken} entities.
 * Provides methods for performing CRUD operations and custom queries.
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    /**
     * Finds an {@link AuthToken} by its token string.
     *
     * @param token the token string to search for
     * @return an {@link Optional} containing the found {@link AuthToken}, or empty if not found
     */
    Optional<AuthToken> findByToken(String token);

    /**
     * Deletes all {@link AuthToken} entities that have an expiration date before specified date.
     *
     * @param dateTime the date before which tokens will be deleted
     * @return the number of tokens deleted
     */
    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}
