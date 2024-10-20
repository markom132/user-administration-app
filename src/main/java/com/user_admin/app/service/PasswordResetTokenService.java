package com.user_admin.app.service;

import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public void deleteResetToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }
}
