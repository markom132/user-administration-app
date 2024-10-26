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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Validated
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    ObjectMapper objectMapper = new ObjectMapper();

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletRequest request) throws MessagingException, IOException {
        Map<String, Object> response = new HashMap<>();
        try {
            response = userService.login(loginRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            response.put("message", "Error occurred during login request");
            response.put("error", e.getMessage());
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(loginRequest, Map.class)));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) throws MessagingException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Authorization header is missing or invalid");
            }

            userService.logout(authorizationHeader);
            return ResponseEntity.ok("Logged out successfully");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.empty());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam @NotBlank(message = "Email cannot be blank")
                                            @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
                                            @Email(message = "Email should be valid") String email,
                                            HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.forgotPassword(email, request);
            return ResponseEntity.status(HttpStatus.OK).body("Password reset email sent");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.empty());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@Validated @RequestBody ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.validatePasswordResetRequest(resetPasswordDTO, request);
            return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully. You can login with your new password now");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(resetPasswordDTO, Map.class)));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/auth/activate-account")
    public ResponseEntity<?> activateAccount(@Validated @RequestBody ActivateAccountDTO activateAccountDTO, HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.validateActivateAccountRequest(activateAccountDTO, request);
            return ResponseEntity.status(HttpStatus.OK).body("Account is activated, you can log in now.");
        } catch (RuntimeException e) {
            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(activateAccountDTO, Map.class)));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/users")
    @JsonView(UserDTO.BasicInfo.class)
    public List<UserDTO> getUsers(@RequestParam(required = true) String accountStatus,
                                  @RequestParam(required = false, defaultValue = "") String name,
                                  @RequestParam(required = false, defaultValue = "") String email) {

        return userService.getUsersByFilters(accountStatus, name, email);
    }

    @PostMapping("/users")
    @JsonView(UserDTO.BasicInfo.class)
    public ResponseEntity<Map<String, Object>> createUser(@Validated @RequestBody UserDTO userDTO,
                                                          HttpServletRequest request) throws MessagingException, IOException {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("message", "Successfully created user " + userDTO.getFirstName().toUpperCase() + " " + userDTO.getLastName().toUpperCase());
            response.put("user", userService.createUser(userDTO, request));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("message", "Error creating user");
            response.put("error", e.getMessage());

            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(userDTO, Map.class)));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users/{id}")
    @JsonView(UserDTO.ExtendedInfo.class)
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id,
                                                          @Validated @RequestBody UserDTO userDTO, HttpServletRequest request) throws MessagingException, IOException {
        Map<String, Object> response = new HashMap<>();

        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO, request);

            response.put("message", "Successfully updated user " + userDTO.getFirstName().toUpperCase() + " " + userDTO.getLastName().toUpperCase());
            response.put("user", updatedUser);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            response.put("message", "Error updating user");
            response.put("error", e.getMessage());

            emailService.sendError(e, request, Optional.of(objectMapper.convertValue(userDTO, Map.class)));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/users/{id}/resend-account-activation-email")
    public ResponseEntity<String> resendActivationEmail(@PathVariable Long id,
                                                        HttpServletRequest request) throws MessagingException, IOException {
        try {
            userService.resendActivationEmail(id, request);
            return ResponseEntity.status(HttpStatus.OK).body("Email send successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/users/{id}/set-account-state")
    public ResponseEntity<String> setState(@PathVariable Long id, HttpServletRequest request) {
        try {
            userService.changeUserStatus(id, request);
            return ResponseEntity.status(HttpStatus.OK).body("Account status changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
