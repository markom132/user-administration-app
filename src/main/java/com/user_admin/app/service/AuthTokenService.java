package com.user_admin.app.service;

import com.user_admin.app.config.JwtUtil;
import com.user_admin.app.exceptions.DatabaseException;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.User;
import com.user_admin.app.repository.AuthTokenRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;
    private final JwtUtil jwtUtil;

    public AuthTokenService(AuthTokenRepository authTokenRepository, JwtUtil jwtUtil) {
        this.authTokenRepository = authTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthToken createAuthToken(String token, User user) {
        try {
            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setUser(user);
            authToken.setCreatedAt(LocalDateTime.now());
            authToken.setLastUsedAt(LocalDateTime.now());

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
            authToken.setExpiresAt(expiresAt);

            return authTokenRepository.save(authToken);
        } catch (DataAccessException e) {
            throw new DatabaseException("Unable to create auth token " + e);
        }
    }

    public void updateToExpired(AuthToken authToken) {
        authToken.setExpiresAt(LocalDateTime.now());
        authTokenRepository.save(authToken);
    }

    public AuthToken findByToken(String token) {
        try {
             return authTokenRepository.findByToken(token).get();
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Token not found" + e);
        }
    }

}
