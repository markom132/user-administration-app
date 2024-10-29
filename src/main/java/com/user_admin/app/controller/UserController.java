package com.user_admin.app.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_admin.app.model.dto.ActivateAccountDTO;
import com.user_admin.app.model.dto.LoginRequestDTO;
import com.user_admin.app.model.dto.ResetPasswordDTO;
import com.user_admin.app.model.dto.UserDTO;
import com.user_admin.app.service.EmailService;
import com.user_admin.app.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling user-related operations such as authentication, account activation, password management, and user information retrieval.
 */
@RestController
@RequestMapping("/api")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final EmailService emailService;
    ObjectMapper objectMapper = new ObjectMapper();

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    /**
     * Handles user login requests.
     *
     * @param loginRequest the login request containing email and password
     * @param request      the HTTP servlet request
     * @return a response entity containing login response or error message
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletRequest request) throws MessagingException, IOException {
        Map<String, Object> response = new HashMap<>();
        try {
            response = userService.login(loginRequest);
            logger.info("User logged in successfully: {}", loginRequest.getEmail());

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            response.put("message", "Error occurred during login request");
            response.put("error", e.getMessage());
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(loginRequest, Map.class)));
            logger.error("Login error for {}: {}", loginRequest.getEmail(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Handles user logout requests.
     *
     * @param request the HTTP servlet request
     * @return a response entity indicating the logout status
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) throws MessagingException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warn("Authorization header is missing or invalid during logout");

                return ResponseEntity.badRequest().body("Authorization header is missing or invalid");
            }

            userService.logout(authorizationHeader);
            logger.info("User logged out successfully");

            return ResponseEntity.ok("Logged out successfully");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.empty());
            logger.error("Logout error: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Handles requests for password reset link.
     *
     * @param email   the email address of the user requesting password reset
     * @param request the HTTP servlet request
     * @return a response entity indicating the status of the request
     */
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam @NotBlank(message = "Email cannot be blank")
                                            @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
                                            @Email(message = "Email should be valid") String email,
                                            HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.forgotPassword(email, request);
            logger.info("Password reset email sent to: {}", email);

            return ResponseEntity.status(HttpStatus.OK).body("Password reset email sent");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.empty());
            logger.error("Error sending password reset email to {}: {}", email, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    /**
     * Handles password reset requests.
     *
     * @param resetPasswordDTO the reset password data transfer object containing new password and password reset token
     * @param request          the HTTP servlet request
     * @return a response entity indicating the status of the reset operation
     */
    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@Validated @RequestBody ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.validatePasswordResetRequest(resetPasswordDTO, request);
            logger.info("Password reset successfully for user: {}", resetPasswordDTO.getEmail());

            return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully. You can login with your new password now");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(resetPasswordDTO, Map.class)));
            logger.error("Error resetting password for {}: {}", resetPasswordDTO.getEmail(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Handles account activation requests.
     *
     * @param activateAccountDTO the account activation data transfer object containing activation token and email
     * @param request            the HTTP servlet request
     * @return a response entity indicating the status of the activation operation
     */
    @PostMapping("/auth/activate-account")
    public ResponseEntity<?> activateAccount(@Validated @RequestBody ActivateAccountDTO activateAccountDTO, HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.validateActivateAccountRequest(activateAccountDTO, request);
            logger.info("Account activated for user: {}", activateAccountDTO.getEmail());

            return ResponseEntity.status(HttpStatus.OK).body("Account is activated, you can log in now.");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(activateAccountDTO, Map.class)));
            logger.error("Error activating account for {}: {}", activateAccountDTO.getEmail(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Retrieves a list of users based on their account status and optional filters.
     *
     * @param accountStatus the status of the accounts to filter
     * @param name          optional filter for user names
     * @param email         optional filter for user emails
     * @return a list of UserDTO objects
     */
    @GetMapping("/users")
    @JsonView(UserDTO.BasicInfo.class)
    public List<UserDTO> getUsers(@RequestParam(required = true) String accountStatus,
                                  @RequestParam(required = false, defaultValue = "") String name,
                                  @RequestParam(required = false, defaultValue = "") String email) {

        return userService.getUsersByFilters(accountStatus, name, email);
    }

    /**
     * Creates a new user account.
     *
     * @param userDTO the user data transfer object containing user information
     * @param request the HTTP servlet request
     * @return a response entity containing success message and created user information
     */
    @PostMapping("/users")
    @JsonView(UserDTO.BasicInfo.class)
    public ResponseEntity<Map<String, Object>> createUser(@Validated @RequestBody UserDTO userDTO,
                                                          HttpServletRequest request) throws MessagingException, IOException {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("message", "Successfully created user " + userDTO.getFirstName().toUpperCase() + " " + userDTO.getLastName().toUpperCase());
            response.put("user", userService.createUser(userDTO, request));
            logger.info("User created: {}", userDTO.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("message", "Error creating user");
            response.put("error", e.getMessage());
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(userDTO, Map.class)));
            logger.error("Error creating user {}: {}", userDTO.getEmail(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves user information by user ID.
     *
     * @param id the ID of the user to retrieve
     * @return a response entity containing user information
     */
    @GetMapping("/users/{id}")
    @JsonView(UserDTO.ExtendedInfo.class)
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
        } catch (RuntimeException e) {
            logger.error("Error retrieving user with ID {}: {}", id, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Updates user information.
     *
     * @param id      the ID of the user to update
     * @param userDTO the user data transfer object containing updated information
     * @param request the HTTP servlet request
     * @return a response entity containing success message and updated user information
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id,
                                                          @Validated @RequestBody UserDTO userDTO, HttpServletRequest request) throws MessagingException, IOException {
        Map<String, Object> response = new HashMap<>();

        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO, request);

            response.put("message", "Successfully updated user " + userDTO.getFirstName().toUpperCase() + " " + userDTO.getLastName().toUpperCase());
            response.put("user", updatedUser);
            logger.info("User updated: {}", userDTO.getEmail());

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            response.put("message", "Error updating user");
            response.put("error", e.getMessage());
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(userDTO, Map.class)));
            logger.error("Error updating user {}: {}", userDTO.getEmail(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Resends the account activation email to the user
     *
     * @param id      the ID of the user to resend activation email
     * @param request the HTTP servlet request
     * @return a response entity indicating the status of the email resend operation
     */
    @PostMapping("/users/{id}/resend-account-activation-email")
    public ResponseEntity<String> resendActivationEmail(@PathVariable Long id,
                                                        HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.resendActivationEmail(id, request);
            logger.info("Activation email resent to user ID: {}", id);

            return ResponseEntity.status(HttpStatus.OK).body("Email send successfully");
        } catch (RuntimeException e) {
            logger.error("Error resending activation email for user ID {}: {}", id, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Changes the account status of the user.
     *
     * @param id      the ID of the user whose account state will be changed
     * @param request the HTTP servlet request
     * @return a response entity indicating the status of the status change operation
     */
    @PostMapping("/users/{id}/set-account-state")
    public ResponseEntity<String> setState(@PathVariable Long id, HttpServletRequest request) {
        try {
            userService.changeUserStatus(id, request);
            logger.info("Account status changed for user ID: {}", id);

            return ResponseEntity.status(HttpStatus.OK).body("Account status changed successfully");
        } catch (RuntimeException e) {
            logger.error("Error changing account status for user ID {}: {}", id, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
