package com.user_admin.app.service;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.DatabaseException;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.User;
import com.user_admin.app.repository.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service class for managing authentication tokens.
 */
@Service
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(AuthTokenService.class);

    public AuthTokenService(AuthTokenRepository authTokenRepository, JwtUtil jwtUtil) {
        this.authTokenRepository = authTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Creates a new authentication token in the database for the given user.
     *
     * @param token the JWT token
     * @param user  the user associated with the token
     * @return the created AuthToken
     * @throws DatabaseException if unable to create the auth token due to database error
     */
    public AuthToken createAuthToken(String token, User user) {
        try {
            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setUser(user);
            authToken.setCreatedAt(LocalDateTime.now());
            authToken.setLastUsedAt(LocalDateTime.now());

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
            authToken.setExpiresAt(expiresAt);

            logger.info("Creating auth token for user: {}", user.getEmail());
            return authTokenRepository.save(authToken);
        } catch (DataAccessException e) {
            logger.error("Database error while creating auth token: {}", e.getMessage());
            throw new DatabaseException("Unable to create auth token " + e);
        }
    }

    /**
     * Marks the given authentication token as expired.
     *
     * @param authToken the token to be updated
     */
    public void updateToExpired(AuthToken authToken) {
        authToken.setExpiresAt(LocalDateTime.now());
        authTokenRepository.save(authToken);
        logger.info("Auth token for user {} marked as expired", authToken.getUser().getEmail());
    }

    /**
     * Finds an authentication token by its string representation.
     *
     * @param token the token string to search for
     * @return the found AuthToken
     * @throws ResourceNotFoundException if the token is not found
     */
    public AuthToken findByToken(String token) {
        try {
            return authTokenRepository.findByToken(token).orElseThrow(() ->
                    new ResourceNotFoundException("Token not found: " + token)
            );
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {},", e.getMessage());
            throw new ResourceNotFoundException("Token not found" + e);
        }
    }

    /**
     * Gets the session timeout duration for the token associated with the request.
     *
     * @param request the HTTP request containing the authorization header
     * @return a string indicating how long the session has been active
     */
    public String getSessionTimeout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = authorizationHeader.substring(7);

        AuthToken authToken = findByToken(token);
        LocalDateTime createdAt = authToken.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();

        Long minutesPassed = ChronoUnit.MINUTES.between(createdAt, now);

        return "The session is active " + minutesPassed + " minutes";
    }

    /**
     * Updates the expiration time for the session associated with the token in the request.
     *
     * @param sessionTimeout the new session timeout in minutes
     * @param httpRequest    the HTTP request containing the authorization header
     */
    public void updateSessionTimeout(Integer sessionTimeout, HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        String token = authorizationHeader.substring(7);

        AuthToken authToken = findByToken(token);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(sessionTimeout);
        authToken.setExpiresAt(expiresAt);

        authTokenRepository.save(authToken);
        logger.info("Session timeout updated for token {}", token);
    }
}
