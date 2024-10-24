package com.user_admin.app.service;

import com.user_admin.app.exceptions.DatabaseException;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.repository.PasswordResetTokenRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void deleteToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }

    public void createPasswordResetToken(String hashedToken, User user) {
        try {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(hashedToken);
            passwordResetToken.setUser(user);
            passwordResetToken.setCreatedAt(LocalDateTime.now());
            passwordResetToken.setExpiresAt(expiresAt);

            passwordResetTokenRepository.save(passwordResetToken);
        } catch (DataAccessException e) {
            throw new DatabaseException("Unable to create password reset token " + e);
        }
    }

    public void createActivateAccountToken(String hashedToken, User user) {
        try {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setActivateAccountToken(hashedToken);
            passwordResetToken.setUser(user);
            passwordResetToken.setCreatedAt(LocalDateTime.now());
            passwordResetToken.setExpiresAt(expiresAt);

            passwordResetTokenRepository.save(passwordResetToken);
        } catch (DataAccessException e) {
            throw new DatabaseException("Unable to create password reset token " + e);
        }
    }

    public List<PasswordResetToken> findAllByUserEmail(String email) {
        return passwordResetTokenRepository.findAllByUserEmail(email);
    }

}
