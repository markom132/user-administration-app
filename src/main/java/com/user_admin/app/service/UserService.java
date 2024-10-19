package com.user_admin.app.service;

import com.user_admin.app.config.JwtUtil;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.repository.AuthTokenRepository;
import com.user_admin.app.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository authTokenRepository;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, AuthTokenRepository authTokenRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.authTokenRepository = authTokenRepository;
    }

    public Map<String, Object> login(Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
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
}
