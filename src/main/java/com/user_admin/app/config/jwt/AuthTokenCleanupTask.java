package com.user_admin.app.config.jwt;

import com.user_admin.app.repository.AuthTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuthTokenCleanupTask {

    private final AuthTokenRepository authTokenRepository;

    public AuthTokenCleanupTask(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    @Scheduled(cron = "0 */15 * * * ?") //executed every 15 minutes
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedTokens = authTokenRepository.deleteByExpiresAtBefore(now);
        System.out.println("Deleted " + deletedTokens + " expired tokens");
    }

}
