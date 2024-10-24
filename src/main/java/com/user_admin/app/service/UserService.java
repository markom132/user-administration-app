package com.user_admin.app.service;

import com.user_admin.app.config.JwtUtil;
import com.user_admin.app.exceptions.DuplicateEmailException;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.model.dto.ActivateAccountDTO;
import com.user_admin.app.model.dto.LoginRequestDTO;
import com.user_admin.app.model.dto.ResetPasswordDTO;
import com.user_admin.app.model.dto.UserDTO;
import com.user_admin.app.model.dto.mappers.UserMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, AuthTokenService authTokenService, EmailService emailService, PasswordResetTokenService passwordResetTokenService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.emailService = emailService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.userMapper = userMapper;
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

            AuthToken authToken = authTokenService.createAuthToken(token, user);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedExpiresAt = authToken.getExpiresAt().format(formatter);

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
        AuthToken authToken = authTokenService.findByToken(token);

        authTokenService.updateToExpired(authToken);
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

        passwordResetTokenService.createPasswordResetToken(hashedToken, user);

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

        List<PasswordResetToken> resetTokens = passwordResetTokenService.findAllByUserEmail(email);

        List<PasswordResetToken> activateAccountTokens = resetTokens.stream().filter(resetToken -> resetToken.getToken() != null).collect(Collectors.toList());


        if (activateAccountTokens.isEmpty()) {
            throw new RuntimeException("No reset tokens for email: " + email);
        }

        PasswordResetToken validToken = activateAccountTokens.stream()
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
        passwordResetTokenService.deleteToken(validToken);
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

    public void validateActivateAccountRequest(ActivateAccountDTO activateAccountDTO) {
        User user = userRepository.findByEmail(activateAccountDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + activateAccountDTO.getEmail()));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("User is already active");
        }

        if (!activateAccountDTO.getPassword().equals(activateAccountDTO.getPasswordConfirmation())) {
            throw new RuntimeException("New password and confirmation password don't match");
        }

        if (!isValidPassword(activateAccountDTO.getPassword())) {
            throw new RuntimeException("Password must contain at least one uppercase letter, one number, and one special character");
        }

        user.setPassword(passwordEncoder.encode(activateAccountDTO.getPassword()));

        activateAccount(user, activateAccountDTO.getActivateAccountToken());
    }

    public void activateAccount(User user, String activateAccountToken) {
        List<PasswordResetToken> passwordResetTokens = passwordResetTokenService.findAllByUserEmail(user.getEmail());

        List<PasswordResetToken> activateAccountTokens = passwordResetTokens.stream().filter(token -> token.getActivateAccountToken() != null).collect(Collectors.toList());

        if (activateAccountTokens.isEmpty()) {
            throw new RuntimeException("No activate account tokens for user: " + user.getEmail());
        }

        PasswordResetToken latestToken = activateAccountTokens.stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No valid active tokens found for email: " + user.getEmail()));

        if (!passwordEncoder.matches(activateAccountToken, latestToken.getActivateAccountToken())) {
            throw new RuntimeException("Invalid activate account token");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        passwordResetTokenService.deleteToken(latestToken);
    }

    public List<UserDTO> getUsersByFilters(String status, String name, String email) {

        String firstName = "";
        String lastName = "";
        if (!name.isEmpty()) {
            String[] chars = name.split(" ", 2);
            firstName = chars[0];
            if (chars.length > 1) {
                lastName = chars[1];
            }
        }

        Optional<List<User>> users = userRepository.findByFilters(UserStatus.valueOf(status), firstName, lastName, email);

        return users.map(userMapper::toDtoList).orElseGet(Collections::emptyList);
    }

    public void validateCreateUserRequest(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exist with email: " + email);
        }
    }

    public UserDTO createUser(UserDTO userDTO) {
        String email = userDTO.getEmail();

        validateCreateUserRequest(email);

        userDTO.setStatus(UserStatus.INACTIVE.name());
        User newUser = userMapper.toEntity(userDTO);

        userRepository.save(newUser);

        String activateToken = KeyGenerators.string().generateKey();
        String hashedToken = passwordEncoder.encode(activateToken);

        passwordResetTokenService.createActivateAccountToken(hashedToken, newUser);

        String activateLink = "http://localhost:8080/api/activate-account/" + activateToken + "/" + email;

        emailService.sendActivateAccountEmail(email, activateLink);
        return userMapper.toDTO(newUser);
    }


    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with ID: " + +id + " not found"));
        return userMapper.toDTO(user);
    }

    public UserDTO updateUser(Long id, UserDTO updatedData) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with ID: " + id + " not found"));

        if (!user.getEmail().equals(updatedData.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(updatedData.getEmail());

            if (userWithSameEmail.isPresent()) {
                throw new DuplicateEmailException("Email " + updatedData.getEmail() + " is already in use by another user");
            }
        }

        user.setFirstName(updatedData.getFirstName().toUpperCase());
        user.setLastName(updatedData.getLastName().toUpperCase());
        user.setEmail(updatedData.getEmail());

        User updateduser = userRepository.save(user);

        return userMapper.toDTO(updateduser);
    }

    public void resendActivationEmail(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with ID: " + id + " not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("User with ID: " + id + " is already active");
        }

        List<PasswordResetToken> passwordResetTokens = passwordResetTokenService.findAllByUserEmail(user.getEmail());

        List<PasswordResetToken> oldActivateAccountTokens = passwordResetTokens.stream().filter(token -> token.getActivateAccountToken() != null).collect(Collectors.toList());

        oldActivateAccountTokens.forEach(passwordResetTokenService::deleteToken);

        String newActivateToken = KeyGenerators.string().generateKey();
        String hashedToken = passwordEncoder.encode(newActivateToken);

        passwordResetTokenService.createPasswordResetToken(hashedToken, user);

        String activateLink = "http://localhost:8080/api/activate-account/" + newActivateToken + "/" + user.getEmail();

        emailService.sendActivateAccountEmail(user.getEmail(), activateLink);
    }

    public void changeUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID: " + id + " not found"));

        if (user.getStatus().equals(UserStatus.ACTIVE)) {
            user.setStatus(UserStatus.INACTIVE);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }
}
