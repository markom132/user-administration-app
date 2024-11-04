package com.user_admin.app.services;

import com.user_admin.app.exceptions.DatabaseException;
import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.repository.PasswordResetTokenRepository;
import com.user_admin.app.service.PasswordResetTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetTokenService passwordResetTokenService;

    private PasswordResetToken token;

    private User user;

    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordResetTokenService = new PasswordResetTokenService(passwordResetTokenRepository, passwordEncoder);
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        token = new PasswordResetToken();
        token.setUser(user);
    }

    @Test
    void deleteToken_ShouldDeleteTokenAndLogInfo() {
        passwordResetTokenService.deleteToken(token);

        verify(passwordResetTokenRepository, times(1)).delete(token);
    }

    @Test
    void deleteToken_NullToken_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            passwordResetTokenService.deleteToken(null);
        });
    }

    @Test
    public void createPasswordResetToken_Success() {
        String hashedToken = "hashedToken";

        passwordResetTokenService.createPasswordResetToken(hashedToken, user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(hashedToken);
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        verify(passwordResetTokenRepository, times(1)).save(argThat(savedToken -> {
            assertEquals(hashedToken, savedToken.getToken());
            assertEquals(user.getId(), savedToken.getUser().getId());
            assertNotNull(savedToken.getCreatedAt());
            assertNotNull(savedToken.getExpiresAt());
            return true;
        }));
    }

    @Test
    public void createPasswordResetToken_DataAccessException() {
        String hashedToken = "hashedToken";

        doThrow(new DataAccessException("Database error") {
        }).when(passwordResetTokenRepository).save(any(PasswordResetToken.class));

        assertThrows(DatabaseException.class, () -> {
            passwordResetTokenService.createPasswordResetToken(hashedToken, user);
        });
    }


    @Test
    void createPasswordResetToken_NullUser_ShouldThrowNullPointerException() {
        String hashedToken = "hashedToken123";

        assertThrows(NullPointerException.class, () -> {
            passwordResetTokenService.createPasswordResetToken(hashedToken, null);
        });
    }

    @Test
    public void createActivateAccountToken_Success() {
        String hashedToken = "hashedActivateToken";

        passwordResetTokenService.createActivateAccountToken(hashedToken, user);

        PasswordResetToken token = new PasswordResetToken();
        token.setActivateAccountToken(hashedToken);
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        verify(passwordResetTokenRepository, times(1)).save(argThat(savedToken -> {
            assertEquals(hashedToken, savedToken.getActivateAccountToken());
            assertEquals(user.getId(), savedToken.getUser().getId());
            assertNotNull(savedToken.getCreatedAt());
            assertNotNull(savedToken.getExpiresAt());
            return true;
        }));
    }

    @Test
    public void createActivateAccountToken_DataAccessException() {
        String hashedToken = "hashedActivateToken";

        doThrow(new DataAccessException("Database error") {}).when(passwordResetTokenRepository).save(any(PasswordResetToken.class));

        assertThrows(DatabaseException.class, () -> {
            passwordResetTokenService.createActivateAccountToken(hashedToken, user);
        });
    }

    @Test
    public void findAllByUserEmail_TokensFound() {
        List<PasswordResetToken> tokens = new ArrayList<>();
        PasswordResetToken token1 = new PasswordResetToken();
        token1.setToken("token1");
        PasswordResetToken token2 = new PasswordResetToken();
        token2.setToken("token2");
        tokens.add(token1);
        tokens.add(token2);

        when(passwordResetTokenRepository.findAllByUserEmail(userEmail)).thenReturn(tokens);

        List<PasswordResetToken> result = passwordResetTokenService.findAllByUserEmail(userEmail);

        assertEquals(2, result.size());
        verify(passwordResetTokenRepository, times(1)).findAllByUserEmail(userEmail);
    }

    @Test
    public void findAllByUserEmail_NoTokensFound() {
        when(passwordResetTokenRepository.findAllByUserEmail(userEmail)).thenReturn(new ArrayList<>());

        List<PasswordResetToken> result = passwordResetTokenService.findAllByUserEmail(userEmail);

        assertTrue(result.isEmpty());
        verify(passwordResetTokenRepository, times(1)).findAllByUserEmail(userEmail);
    }

}
