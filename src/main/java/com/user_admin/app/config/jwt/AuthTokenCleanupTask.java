package com.user_admin.app.config.jwt;

import com.user_admin.app.repository.AuthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled task for cleaning up expired authentication tokens.
 * This task runs periodically and removes tokens that have passed their expiration date
 * from the database to maintain a clean and efficient token store
 */
@Component
public class AuthTokenCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenCleanupTask.class);
    private final AuthTokenRepository authTokenRepository;


    /**
     * Constructs an AuthTokenCleanupTask with the given AuthTokenRepository.
     *
     * @param authTokenRepository the repository to access and delete expired tokens
     */
    public AuthTokenCleanupTask(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    /**
     * Scheduled method to delete expired tokens every 15 minutes.
     * Uses the current timestamp to find and remove tokens that expired before the current time.
     * Logs the number of deleted tokens after each execution.
     */
    @Scheduled(cron = "0 */15 * * * ?") //executed every 15 minutes
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // Delete tokens that have expired before the current time
        int deletedTokens = authTokenRepository.deleteByExpiresAtBefore(now);

        logger.info("Deleted {} expired tokens at {}", deletedTokens, now);
    }

}
