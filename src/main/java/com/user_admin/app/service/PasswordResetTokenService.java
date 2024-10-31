package com.user_admin.app.service;

import com.user_admin.app.exceptions.DatabaseException;
import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing password reset and account activation tokens.
 */
@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(PasswordResetTokenService.class);

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Deletes a specific password reset token.
     *
     * @param token the token to delete
     */
    public void deleteToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
        logger.info("Deleted password reset token for user ID: {}", token.getUser().getId());
    }

    /**
     * Creates and saves a new password reset token.
     *
     * @param hashedToken the hashed token value
     * @param user        the user associated with the token
     * @throws DataAccessException if there is an issue accessing the database
     */
    public void createPasswordResetToken(String hashedToken, User user) {
        try {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(hashedToken);
            passwordResetToken.setUser(user);
            passwordResetToken.setCreatedAt(LocalDateTime.now());
            passwordResetToken.setExpiresAt(expiresAt);

            passwordResetTokenRepository.save(passwordResetToken);
            logger.info("Created password reset token for user ID: {}", user.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to create a password reset token for user ID: {}", user.getId(), e);
            throw new DatabaseException("Unable to create password reset token " + e);
        }
    }

    /**
     * Creates and saves a new account activation token.
     *
     * @param hashedToken the hashed token value
     * @param user        the user associated with the token
     * @throws DataAccessException if there is an issue accessing the database
     */
    public void createActivateAccountToken(String hashedToken, User user) {
        try {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setActivateAccountToken(hashedToken);
            passwordResetToken.setUser(user);
            passwordResetToken.setCreatedAt(LocalDateTime.now());
            passwordResetToken.setExpiresAt(expiresAt);

            passwordResetTokenRepository.save(passwordResetToken);
            logger.info("Created account activation token for user ID: {}", user.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to create account activation token for user ID: {}", user.getId(), e);
            throw new DatabaseException("Unable to create password reset token " + e);
        }
    }

    /**
     * Retrieves all password reset tokens associated with a given user email.
     *
     * @param email the email of the user
     * @return a list of PasswordResetToken objects
     */
    public List<PasswordResetToken> findAllByUserEmail(String email) {
        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAllByUserEmail(email);
        logger.info("Found {} tokens for email: {}", tokens.size(), email);
        return tokens;
    }

}
