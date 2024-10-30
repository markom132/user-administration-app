package com.user_admin.app.repository;

import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link PasswordResetToken} entities.
 * Provides methods for performing CRUD operations and custom queries related to password reset tokens.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a {@link PasswordResetToken} by its token string.
     *
     * @param token the token string to search for
     * @return an {@link Optional} containing the found {@link PasswordResetToken}, or empty if not found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Finds all {@link PasswordResetToken} entities associated with a specific user's email.
     *
     * @param email the email of the user whose tokens are to be found
     * @return a list of {@link PasswordResetToken} associated with the given email
     */
    List<PasswordResetToken> findAllByUserEmail(String email);

    /**
     * Deletes a {@link PasswordResetToken} by its token string.
     *
     * @param token the token string of the {@link PasswordResetToken} to delete
     */
    void deleteByToken(String token);

    /**
     * Deletes all {@link PasswordResetToken} entities associated with a specific user.
     *
     * @param user the {@link User} whose tokens are to be deleted
     */
    void deleteByUser(User user);
}
