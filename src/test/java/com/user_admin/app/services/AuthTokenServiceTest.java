package com.user_admin.app.services;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.DatabaseException;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.User;
import com.user_admin.app.repository.AuthTokenRepository;
import com.user_admin.app.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    private AuthTokenService authTokenService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authTokenService = new AuthTokenService(authTokenRepository, jwtUtil);
    }


    @Test
    void createAuthToken_Success() {
        String token = "sampleToken123";
        User user = new User();
        user.setEmail("test@example.com");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(30);

        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUser(user);
        authToken.setCreatedAt(now);
        authToken.setLastUsedAt(now);
        authToken.setExpiresAt(expiresAt);

        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(authToken);

        AuthToken createdAuthToken = authTokenService.createAuthToken(token, user);

        assertNotNull(createdAuthToken, "AuthToken should not be null after save");
        assertEquals(token, createdAuthToken.getToken(), "Token should match input token");
        assertEquals(user, createdAuthToken.getUser(), "User should match input user");
        assertEquals(expiresAt.getMinute(), createdAuthToken.getExpiresAt().getMinute(), "Expires at time should be 30 minutes later");

        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    void createAuthToken_DatabaseException() {
        String token = "sampleToken123";
        User user = new User();
        user.setEmail("test@example.com");

        when(authTokenRepository.save(any(AuthToken.class))).thenThrow(new DataAccessException("Database error") {
        });

        DatabaseException exception = assertThrows(DatabaseException.class, () -> authTokenService.createAuthToken(token, user));
        assertTrue(exception.getMessage().contains("Unable to create auth token"));

        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    void updateToExpired_Success() {
        User user = new User();
        user.setEmail("test@example.com");

        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(authToken);

        authTokenService.updateToExpired(authToken);

        assertNotNull(authToken.getExpiresAt());
        assertEquals(LocalDateTime.now().getMinute(), authToken.getExpiresAt().getMinute());

        verify(authTokenRepository, times(1)).save(authToken);
    }

    @Test
    void updateToExpired_NullAuthToken() {
        assertThrows(NullPointerException.class, () -> authTokenService.updateToExpired(null));
    }

    @Test
    void updateToExpired_DatabaseException() {
        User user = new User();
        user.setEmail("test@example.com");

        AuthToken authToken = new AuthToken();
        authToken.setUser(user);

        when(authTokenRepository.save(any(AuthToken.class))).thenThrow(new DataAccessException("Database error") {
        });

        assertThrows(DataAccessException.class, () -> authTokenService.updateToExpired(authToken));

        verify(authTokenRepository, times(1)).save(authToken);
    }

    @Test
    void findByToken_Success() {
        String token = "sampleToken123";
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(authToken));

        AuthToken foundAuthToken = authTokenService.findByToken(token);

        assertNotNull(foundAuthToken);
        assertEquals(token, foundAuthToken.getToken());

        verify(authTokenRepository, times(1)).findByToken(token);
    }

    @Test
    void findByToken_TokenNotFound() {
        String token = "nonExistentToken";

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authTokenService.findByToken(token);
        });
        assertTrue(exception.getMessage().contains("Token not found: " + token));

        verify(authTokenRepository, times(1)).findByToken(token);
    }

    @Test
    void findByToken_DatabaseException() {
        String token = "sampleToken123";

        when(authTokenRepository.findByToken(anyString())).thenThrow(new DataAccessException("Database error") {
        });

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authTokenService.findByToken(token);
        });
        assertTrue(exception.getMessage().contains("Database error"));

        verify(authTokenRepository, times(1)).findByToken(token);
    }

    @Test
    void getSessionTimeout_Success() {
        String token = "Bearer sampleToken123";
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(10);

        AuthToken authToken = new AuthToken();
        authToken.setToken("sampleToken123");
        authToken.setCreatedAt(createdAt);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(authTokenRepository.findByToken("sampleToken123")).thenReturn(Optional.of(authToken));

        String result = authTokenService.getSessionTimeout(request);

        assertEquals("The session is active 10 minutes", result);

        verify(authTokenRepository, times(1)).findByToken("sampleToken123");
    }

    @Test
    void getSessionTimeout_TokenNotFound() {
        String token = "Bearer sampleToken123";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(authTokenRepository.findByToken("sampleToken123")).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authTokenService.getSessionTimeout(request);
        });
        assertEquals("Token not found: sampleToken123", exception.getMessage());

        verify(authTokenRepository, times(1)).findByToken("sampleToken123");
    }

    @Test
    void getSessionTimeout_MalformedAuthorizationHeader() {
        String token = "sampleToken123";

        when(request.getHeader("Authorization")).thenReturn(token);

        assertThrows(ResourceNotFoundException.class, () -> {
            authTokenService.getSessionTimeout(request);
        });
    }

    @Test
    void updateSessionTimeout_Success() {
        String token = "Bearer sampleToken123";
        Integer sessionTimeout = 30;
        LocalDateTime createdAt = LocalDateTime.now();

        AuthToken authToken = new AuthToken();
        authToken.setToken("sampleToken123");
        authToken.setExpiresAt(createdAt.plusMinutes(15));

        when(request.getHeader("Authorization")).thenReturn(token);
        when(authTokenRepository.findByToken("sampleToken123")).thenReturn(Optional.of(authToken));

        authTokenService.updateSessionTimeout(sessionTimeout, request);

        assertNotNull(authToken.getExpiresAt());
        assertEquals(createdAt.plusMinutes(sessionTimeout).getMinute(), authToken.getExpiresAt().getMinute());

        verify(authTokenRepository, times(1)).save(authToken);
    }

    @Test
    void updateSessionTimeout_TokenNotFound() {
        String token = "Bearer sampleToken123";
        Integer sessionTimeout = 30;

        when(request.getHeader("Authorization")).thenReturn(token);
        when(authTokenRepository.findByToken("sampleToken123")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authTokenService.updateSessionTimeout(sessionTimeout, request);
        });
        assertTrue(exception.getMessage().contains("Token not found"));

        verify(authTokenRepository, times(1)).findByToken("sampleToken123");
    }

    @Test
    void updateSessionTimeout_DatabaseException() {
        String token = "Bearer sampleToken123";
        Integer sessionTimeout = 30;

        AuthToken authToken = new AuthToken();
        authToken.setToken("sampleToken123");

        when(request.getHeader("Authorization")).thenReturn(token);
        when(authTokenRepository.findByToken("sampleToken123")).thenReturn(Optional.of(authToken));
        when(authTokenRepository.save(any(AuthToken.class))).thenThrow(new DataAccessException("Database error") {
        });

        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            authTokenService.updateSessionTimeout(sessionTimeout, request);
        });
        assertTrue(exception.getMessage().contains("Unable to update session timeout"));

        verify(authTokenRepository, times(1)).findByToken("sampleToken123");
        verify(authTokenRepository, times(1)).save(authToken);
    }

}
