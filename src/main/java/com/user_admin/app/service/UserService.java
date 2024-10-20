package com.user_admin.app.service;

import com.user_admin.app.config.JwtUtil;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.model.dto.LoginRequestDTO;
import com.user_admin.app.model.dto.ResetPasswordDTO;
import com.user_admin.app.repository.AuthTokenRepository;
import com.user_admin.app.repository.PasswordResetTokenRepository;
import com.user_admin.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenService passwordResetTokenService;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, AuthTokenRepository authTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService, PasswordResetTokenService passwordResetTokenService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.authTokenRepository = authTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    public Map<String, Object> login(LoginRequestDTO loginRequest) {
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            if (!user.getStatus().equals(UserStatus.ACTIVE)) {
                throw new RuntimeException("User account is inactive");
            }

            String token = jwtUtil.generateToken(user);

            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setUser(user);
            authToken.setCreatedAt(LocalDateTime.now());
            authToken.setLastUsedAt(LocalDateTime.now());

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
            authToken.setExpiresAt(expiresAt);
            authTokenRepository.save(authToken);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedExpiresAt = expiresAt.format(formatter);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("status", String.valueOf(user.getStatus()));
            response.put("token", token);
            response.put("expiresAt", formattedExpiresAt);

            return response;
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public void logout(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        authToken.setExpiresAt(LocalDateTime.now());
        authTokenRepository.save(authToken);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new RuntimeException("User account is not active");
        }

        String resetToken = KeyGenerators.string().generateKey();
        String hashedToken = passwordEncoder.encode(resetToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(hashedToken);
        passwordResetToken.setUser(user);
        passwordResetToken.setCreatedAt(LocalDateTime.now());
        passwordResetToken.setExpiresAt(expiresAt);

        passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = "http://localhost:8080/api/reset-password/" + resetToken + "/" + email;

        emailService.sendResetPasswordEmail(email, resetLink);
    }

    public void validatePasswordResetRequest(ResetPasswordDTO resetPasswordDTO) {
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmationPassword())) {
            throw new RuntimeException("New password and confirmation password don't match");
        }

        if (!isValidPassword(resetPasswordDTO.getNewPassword())) {
            throw new RuntimeException("Password must contain at least one uppercase letter, one number, and one special character");
        }

        String email = resetPasswordDTO.getEmail();
        String token = resetPasswordDTO.getToken();

        List<PasswordResetToken> resetTokens = passwordResetTokenRepository.findAllByUserEmail(email);

        if (resetTokens.isEmpty()) {
            throw new RuntimeException("No reset tokens for email: " + email);
        }

        PasswordResetToken validToken = resetTokens.stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No valid reset tokens found for email: " + email));

        if (!passwordEncoder.matches(token, validToken.getToken())) {
            throw new RuntimeException("Invalid reset token");
        }

        if (validToken.getUser().getStatus().equals(UserStatus.INACTIVE)) {
            throw new RuntimeException("User is inactive: " + validToken.getUser().getEmail());
        }

        if (validToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token is expired");
        }

        updatePassword(email, resetPasswordDTO.getNewPassword());
        passwordResetTokenService.deleteResetToken(validToken);
    }

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordRegex);
    }

}
