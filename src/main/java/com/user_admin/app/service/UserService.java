package com.user_admin.app.service;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.DuplicateEmailException;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.*;
import com.user_admin.app.model.dto.ActivateAccountDTO;
import com.user_admin.app.model.dto.LoginRequestDTO;
import com.user_admin.app.model.dto.ResetPasswordDTO;
import com.user_admin.app.model.dto.UserDTO;
import com.user_admin.app.model.dto.mappers.UserMapper;
import com.user_admin.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service class for managing user-related operations such as login, account activation,
 * password reset, and user data retrieval.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final UserChangeLogService userChangeLogService;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, AuthTokenService authTokenService, EmailService emailService, PasswordResetTokenService passwordResetTokenService, UserMapper userMapper, UserChangeLogService userChangeLogService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.emailService = emailService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.userMapper = userMapper;
        this.userChangeLogService = userChangeLogService;
    }

    /**
     * Authenticates a user using their email and password and generates a JWT token if successful.
     *
     * @param loginRequest DTO containing email and password for login
     * @return Map containing user details and JWT token
     */
    public Map<String, Object> login(LoginRequestDTO loginRequest) {
        logger.info("Attempting to login userwith email: {}", loginRequest.getEmail());
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // Authenticate user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            if (!user.getStatus().equals(UserStatus.ACTIVE)) {
                throw new RuntimeException("User account is inactive");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            AuthToken authToken = authTokenService.createAuthToken(token, user);

            // Format expiration date and prepare response
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

            logger.info("Login successful for user: {}", user.getEmail());
            return response;
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for email: {}", loginRequest.getEmail(), e);
            throw new RuntimeException("Invalid credentials");
        }
    }

    /**
     * Logs out the user by marking the JWT token as expired.
     *
     * @param authorizationHeader authorization header containing JWT token
     */
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ") || authorizationHeader.length() < 8) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        logger.info("Logging out user with token: {}", authorizationHeader);
        String token = authorizationHeader.substring(7);
        AuthToken authToken = authTokenService.findByToken(token);

        authTokenService.updateToExpired(authToken);
        logger.info("Token {} marked as expired", token);
    }

    /**
     * Sends a password reset email to the user if their account is active.
     *
     * @param email   User's email.
     * @param request HTTP request with authorization details
     */
    @Transactional
    public void forgotPassword(String email, HttpServletRequest request) {
        logger.info("Processing forgot password request for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            logger.warn("Account is inactive for user with email: {}", email);
            throw new RuntimeException("User account is not active");
        }

        // Generates reset token and save hashed version
        String resetToken = KeyGenerators.string().generateKey();
        String hashedToken = passwordEncoder.encode(resetToken);

        passwordResetTokenService.createPasswordResetToken(hashedToken, user);

        // Build reset link
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ") || authorizationHeader.length() < 8) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }

        String jwtToken = authorizationHeader.substring(7);

        String resetLink = "http://localhost:8080/api/reset-password/" + resetToken + "/" + email + "/" + jwtToken;

        // Send reset email
        emailService.sendResetPasswordEmail(email, resetLink);
        logger.info("Password reset link sent to email: {}", email);
    }

    /**
     * Validates and updates user's password based on reset token.
     *
     * @param resetPasswordDTO DTO containing reset token, email, and new password
     * @param request          HTTP request with authorization header
     */
    public void validatePasswordResetRequest(ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        logger.info("Validating password reset request for email: {}", resetPasswordDTO.getEmail());

        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmationPassword())) {
            logger.error("Password confirmation does not match for email: {}", resetPasswordDTO.getEmail());
            throw new RuntimeException("New password and confirmation password don't match");
        }

        if (!isValidPassword(resetPasswordDTO.getNewPassword())) {
            logger.error("Invalid password format for email: {}", resetPasswordDTO.getEmail());
            throw new RuntimeException("Password must contain at least one uppercase letter, one number, and one special character");
        }

        String email = resetPasswordDTO.getEmail();
        String resetToken = resetPasswordDTO.getToken();

        // Validate resetToken
        List<PasswordResetToken> activateAccountTokens = passwordResetTokenService
                .findAllByUserEmail(email)
                .stream()
                .filter(token -> token.getToken() != null)
                .toList();

        if (activateAccountTokens.isEmpty()) {
            logger.error("No reset tokens found for email: {}", email);
            throw new RuntimeException("No reset tokens for email: " + email);
        }

        PasswordResetToken validToken = activateAccountTokens.stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No valid reset tokens found for email: " + email));

        if (!passwordEncoder.matches(resetToken, validToken.getToken())) {
            logger.error("Invalid reset token for email: {}", email);
            throw new RuntimeException("Invalid reset resetToken");
        }

        if (validToken.getUser().getStatus().equals(UserStatus.INACTIVE)) {
            logger.warn("Inactive user attempted password reset: {}", email);
            throw new RuntimeException("User is inactive: " + validToken.getUser().getEmail());
        }

        if (validToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.error("Expired reset token for email: {}", email);
            throw new RuntimeException("Reset resetToken is expired");
        }

        // Update password
        updatePassword(email, resetPasswordDTO.getNewPassword(), request);
        passwordResetTokenService.deleteToken(validToken);
        logger.info("Password reset successful for email: {}", email);
    }

    /**
     * Updates the user's password.
     *
     * @param email       user's email
     * @param newPassword new password to set
     * @param request     HTTP request with authorization details
     */
    public void updatePassword(String email, String newPassword, HttpServletRequest request) {
        logger.info("Updating password for user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        String oldPassword = user.getPassword();

        user.setPassword(encodedNewPassword);
        userRepository.save(user);
        logger.info("Password updates for user with email: {}", email);

        if (!passwordEncoder.matches(oldPassword, encodedNewPassword)) {
            UserChangeLog changeLog = userChangeLogService.fillUserChangeLogDTO("password", oldPassword, encodedNewPassword);
            userChangeLogService.logChange(changeLog, user, request);
        }
    }

    /**
     * Validates password format to ensure it meets security requirements.
     *
     * @param password password to validate
     * @return true if password meets requirements; false otherwise
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordRegex);
    }

    /**
     * Validates and processes an account activation request.
     *
     * @param activateAccountDTO DTO containing activation request details
     * @param request            HTTP request for logging
     * @throws RuntimeException if user not found, already active, or validation fails
     */
    public void validateActivateAccountRequest(ActivateAccountDTO activateAccountDTO, HttpServletRequest request) {
        logger.info("Validating activation request for email: {}", activateAccountDTO.getEmail());

        User user = userRepository.findByEmail(activateAccountDTO.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", activateAccountDTO.getEmail());
                    return new RuntimeException("User not found with email: " + activateAccountDTO.getEmail());
                });

        if (user.getStatus() == UserStatus.ACTIVE) {
            logger.warn("User with email {} is already active", activateAccountDTO.getEmail());
            throw new RuntimeException("User is already active");
        }

        if (!activateAccountDTO.getPassword().equals(activateAccountDTO.getPasswordConfirmation())) {
            logger.warn("Password confirmation mismatch for email {}", activateAccountDTO.getEmail());
            throw new RuntimeException("New password and confirmation password don't match");
        }

        if (!isValidPassword(activateAccountDTO.getPassword())) {
            logger.warn("Password does not meet complexity requirements for email: {}", activateAccountDTO.getEmail());
            throw new RuntimeException("Password must contain at least one uppercase letter, one number, and one special character");
        }

        user.setPassword(passwordEncoder.encode(activateAccountDTO.getPassword()));

        UserChangeLog changeLog = userChangeLogService.fillUserChangeLogDTO("password", null, user.getPassword());
        userChangeLogService.logChange(changeLog, user, request);


        activateAccount(user, activateAccountDTO.getActivateAccountToken(), request);
        logger.info("Activation request validated successfully for email: {}", activateAccountDTO.getEmail());
    }

    /**
     * Activates a user account based on a valid activation token.
     *
     * @param user                 the user whose account is being activated
     * @param activateAccountToken token used to confirm account activation
     * @param request              HTTP request for logging
     * @throws RuntimeException if token is invalid or expired
     */
    public void activateAccount(User user, String activateAccountToken, HttpServletRequest request) {
        logger.info("Activating account for user email: {}", user.getEmail());

        List<PasswordResetToken> activateAccountTokens = passwordResetTokenService
                .findAllByUserEmail(user.getEmail())
                .stream()
                .filter(token -> token.getActivateAccountToken() != null)
                .toList();

        if (activateAccountTokens.isEmpty()) {
            logger.error("No activate account tokens found for user: {}", user.getEmail());
            throw new RuntimeException("No activate account tokens for user: " + user.getEmail());
        }

        PasswordResetToken latestToken = activateAccountTokens.stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No valid active tokens found for email: " + user.getEmail()));

        if (!passwordEncoder.matches(activateAccountToken, latestToken.getActivateAccountToken())) {
            logger.error("Invalid activate account token for user: {}", user.getEmail());
            throw new RuntimeException("Invalid activate account token");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        UserChangeLog changeLog = userChangeLogService.fillUserChangeLogDTO("status", String.valueOf(UserStatus.INACTIVE), String.valueOf(UserStatus.ACTIVE));
        userChangeLogService.logChange(changeLog, user, request);


        passwordResetTokenService.deleteToken(latestToken);
        logger.info("Account activated for user: {}", user.getEmail());
    }

    /**
     * Retrieves a list of users filtered by status, name, and email.
     *
     * @param status user status to filter by
     * @param name   partial/full name of the user to filter by
     * @param email  email to filter by
     * @return list of UserDTOs that match the specified filters
     */
    public List<UserDTO> getUsersByFilters(String status, String name, String email) {
        if (status == null || status.isEmpty()) {
            throw new RuntimeException("Status must not be empty");
        }
        logger.info("Fetching users by filters - Status: {}, Name: {}, Email: {}", status, name, email);

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

        logger.info("Retrieved {} users matching the filters", users.map(List::size).orElse(0));
        return users.map(userMapper::toDtoList).orElseGet(Collections::emptyList);
    }

    /**
     * Validates that a user does not already exist with specified email.
     *
     * @param email email to validate
     * @throws RuntimeException if a user with specified email already exists
     */
    public void validateCreateUserRequest(String email) {
        logger.info("Validating create user request for email: {}", email);
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("User already exists with email: {}", email);
            throw new RuntimeException("User already exist with email: " + email);
        }
    }

    /**
     * Creates a new user and sends an activation email.
     *
     * @param userDTO DTO containing user details
     * @param request HTTP request for retrieving authorization token
     * @return UserDTO containing details of the newly created user
     */
    public UserDTO createUser(UserDTO userDTO, HttpServletRequest request) {
        logger.info("Creating new user with email: {}", userDTO.getEmail());

        String email = userDTO.getEmail();
        validateCreateUserRequest(email);

        userDTO.setStatus(UserStatus.INACTIVE.name());
        User newUser = userMapper.toEntity(userDTO);
        userRepository.save(newUser);

        String activateToken = KeyGenerators.string().generateKey();
        String hashedToken = passwordEncoder.encode(activateToken);
        passwordResetTokenService.createActivateAccountToken(hashedToken, newUser);

        String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = authorizationHeader.substring(7);

        String activateLink = createLink(activateToken, email, jwtToken);

        emailService.sendActivateAccountEmail(email, activateLink);
        logger.info("Activation email sent to {}", email);
        return userMapper.toDTO(newUser);
    }

    /**
     * Retrieves user details based on the user ID.
     *
     * @param id ID of the user to search for
     * @return UserDTO containing user details
     * @throws ResourceNotFoundException if user with specified ID is not found
     */
    public UserDTO getUserById(Long id) {
        logger.info("Retrieving user with ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with ID: " + +id + " not found"));
        logger.info("User with ID: {} retrieved successfully", id);
        return userMapper.toDTO(user);
    }

    /**
     * Updates user information for a specified user ID.
     *
     * @param id          the unique ID of the user
     * @param updatedData UserDTO containing updated information
     * @param request     HTTP request for logging
     * @return updated UserDTO
     * @throws ResourceNotFoundException if user with specified ID is not found
     * @throws DuplicateEmailException   if email is already in use by another user
     */
    public UserDTO updateUser(Long id, UserDTO updatedData, HttpServletRequest request) {
        logger.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with ID: " + id + " not found"));

        if (!user.getEmail().equals(updatedData.getEmail())) {
            userRepository.findByEmail(updatedData.getEmail())
                    .ifPresent(existingUser -> {
                        logger.warn("Email {} is already in use", updatedData.getEmail());
                        throw new DuplicateEmailException("Email " + updatedData.getEmail() + " is already in use by another user");
                    });
        }

        // Log changes for each updated field
        UserChangeLog changeLog = new UserChangeLog();

        if (!user.getFirstName().equals(updatedData.getFirstName().toUpperCase())) {
            changeLog = userChangeLogService.fillUserChangeLogDTO("firstName", user.getFirstName(), updatedData.getFirstName().toUpperCase());
            userChangeLogService.logChange(changeLog, user, request);
        }

        if (!user.getLastName().equals(updatedData.getLastName().toUpperCase())) {
            changeLog = userChangeLogService.fillUserChangeLogDTO("lastName", user.getLastName(), updatedData.getLastName().toUpperCase());
            userChangeLogService.logChange(changeLog, user, request);
        }

        if (!user.getEmail().equals(updatedData.getEmail())) {
            changeLog = userChangeLogService.fillUserChangeLogDTO("email", user.getEmail(), updatedData.getEmail());
            userChangeLogService.logChange(changeLog, user, request);
        }


        // Update and save user
        user.setFirstName(updatedData.getFirstName().toUpperCase());
        user.setLastName(updatedData.getLastName().toUpperCase());
        user.setEmail(updatedData.getEmail());

        User updateduser = userRepository.save(user);
        logger.info("User with ID: {} updated successfully", id);
        return userMapper.toDTO(updateduser);
    }

    /**
     * Resends the account activation email to the user with the specified ID.
     *
     * @param id      the unique ID of the user
     * @param request HTTP request for obtaining the authorization token
     * @throws ResourceNotFoundException if user with specified ID is not found
     * @throws RuntimeException          if user is already active
     */
    public void resendActivationEmail(Long id, HttpServletRequest request) {
        logger.info("Resending activation email for user with ID: {}", id);

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with ID: " + id + " not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            logger.warn("User with ID: {} is already active", id);
            throw new RuntimeException("User with ID: " + id + " is already active");
        }

        // Delete old tokens and create a new one
        List<PasswordResetToken> passwordResetTokens = passwordResetTokenService.findAllByUserEmail(user.getEmail());
        passwordResetTokens.stream()
                .filter(token -> token.getActivateAccountToken() != null)
                .forEach(passwordResetTokenService::deleteToken);


        String newActivateToken = KeyGenerators.string().generateKey();
        String hashedToken = passwordEncoder.encode(newActivateToken);

        passwordResetTokenService.createActivateAccountToken(hashedToken, user);

        String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = authorizationHeader.substring(7);

        String activateLink = createLink(newActivateToken, user.getEmail(), jwtToken);

        emailService.sendActivateAccountEmail(user.getEmail(), activateLink);
        logger.info("Activation email resent to user with email: {}", user.getEmail());
    }

    /**
     * Changes the status of a user (active/inactive).
     *
     * @param id      the unique ID of the user
     * @param request HTTP request for logging
     * @throws ResourceNotFoundException if user with specified ID is not found
     */
    public void changeUserStatus(Long id, HttpServletRequest request) {
        logger.info("Changing status for user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID: " + id + " not found"));

        UserChangeLog changeLog = new UserChangeLog();

        if (user.getStatus().equals(UserStatus.ACTIVE)) {
            user.setStatus(UserStatus.INACTIVE);
            changeLog = userChangeLogService.fillUserChangeLogDTO("status", String.valueOf(UserStatus.ACTIVE), String.valueOf(UserStatus.INACTIVE));
            logger.info("User with ID: {} deactivated", id);
        } else {
            user.setStatus(UserStatus.ACTIVE);
            changeLog = userChangeLogService.fillUserChangeLogDTO("status", String.valueOf(UserStatus.INACTIVE), String.valueOf(UserStatus.ACTIVE));
            logger.info("User with ID: {} activated", id);
        }

        userChangeLogService.logChange(changeLog, user, request);
        userRepository.save(user);
    }

    /**
     * Constructs an activation link.
     *
     * @param token    the activation token
     * @param email    the email of the user
     * @param jwtToken JWT authorization token
     * @return a formatted activation link
     */
    public String createLink(String token, String email, String jwtToken) {
        return "http://localhost:8080/api/activate-account/" + token + "/" + email + "/" + jwtToken;
    }
}
