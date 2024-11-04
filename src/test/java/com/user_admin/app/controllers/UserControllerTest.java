package com.user_admin.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.model.dto.ActivateAccountDTO;
import com.user_admin.app.model.dto.LoginRequestDTO;
import com.user_admin.app.model.dto.ResetPasswordDTO;
import com.user_admin.app.model.dto.UserDTO;
import com.user_admin.app.model.dto.mappers.UserMapper;
import com.user_admin.app.repository.RequestResponseLogRepository;
import com.user_admin.app.repository.UserRepository;
import com.user_admin.app.service.EmailService;
import com.user_admin.app.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RequestResponseLogRepository requestResponseLogRepository;

    @MockBean
    private HttpServletRequest request;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO loginRequestDTO;

    private User mockUser;

    private ResetPasswordDTO validResetPasswordDTO;

    private ActivateAccountDTO validActivateAccountDTO;

    private List<UserDTO> mockUsers;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("john.doe@example.com");
        loginRequestDTO.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword(passwordEncoder.encode("password123"));
        mockUser.setStatus(UserStatus.ACTIVE);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        validResetPasswordDTO = new ResetPasswordDTO();
        validResetPasswordDTO.setEmail("user@example.com");
        validResetPasswordDTO.setToken("validToken");
        validResetPasswordDTO.setNewPassword("newStrongPassword123!");
        validResetPasswordDTO.setConfirmationPassword("newStrongPassword123!");

        validActivateAccountDTO = new ActivateAccountDTO();
        validActivateAccountDTO.setEmail("user@example.com");
        validActivateAccountDTO.setActivateAccountToken("validToken");
        validActivateAccountDTO.setPassword("newStrongPassword123!");
        validActivateAccountDTO.setPasswordConfirmation("newStrongPassword123!");

        mockUsers = new ArrayList<>();

        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");
        user1.setStatus(UserStatus.ACTIVE.name());

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setStatus(UserStatus.INACTIVE.name());

        mockUsers.add(user1);
        mockUsers.add(user2);

        when(userService.login(any(LoginRequestDTO.class))).thenReturn(createSuccessfulLoginResponse(mockUser));
        when(jwtUtil.validateToken(anyString(), any())).thenReturn(true);
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.ofNullable(mockUser));

        doNothing().when(userService).logout(any(String.class));
    }

    private Map<String, Object> createSuccessfulLoginResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Login successful");
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("status", String.valueOf(user.getStatus()));
        response.put("token", "mockedJwtToken");
        response.put("expiresAt", "2024-11-02 12:00:00");
        return response;
    }

    @Test
    void login_Success() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void login_Failure() throws Exception {
        when(userService.login(any(LoginRequestDTO.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error occurred during login request"))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void logout_Successful() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(userService).logout("Bearer mockedJwtToken");
    }

    @Test
    @WithMockUser(username = "testuser")
    void logout_MissingAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("JWT token is missing or invalid"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void logout_ErrorDuringLogout() throws Exception {
        String jwtToken = "Bearer validToken";
        when(request.getHeader("Authorization")).thenReturn(jwtToken);

        doThrow(new RuntimeException("Logout failed")).when(userService).logout(anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Logout failed"));

        verify(emailService).sendError(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void forgotPassword_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";
        String email = "test@example.com";

        doNothing().when(userService).forgotPassword(eq(email), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", email)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset email sent"));

        verify(userService).forgotPassword(eq(email), any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void forgotPassword_InvalidEmailFormat() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";
        String invalidEmail = "invalid-email";

        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", invalidEmail)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email should be valid")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void forgotPassword_ErrorDuringProcessing() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";
        String email = "test@example.com";

        doThrow(new RuntimeException("Failed to send email")).when(userService).forgotPassword(eq(email), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", email)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to send email"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void resetPassword_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(validResetPasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully. You can login with your new password now"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void resetPassword_InvalidEmailFormat() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        ResetPasswordDTO invalidResetPasswordDTO = new ResetPasswordDTO();
        invalidResetPasswordDTO.setEmail("invalid-email");
        invalidResetPasswordDTO.setToken("");
        invalidResetPasswordDTO.setNewPassword("short");
        invalidResetPasswordDTO.setConfirmationPassword("short");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(invalidResetPasswordDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email should be valid")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void resetPassword_InvalidToken() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        ResetPasswordDTO invalidTokenDTO = new ResetPasswordDTO();
        invalidTokenDTO.setEmail("user@example.com");
        invalidTokenDTO.setToken("invalidToken");
        invalidTokenDTO.setNewPassword("newStrongPassword123!");
        invalidTokenDTO.setConfirmationPassword("newStrongPassword123!");

        doThrow(new RuntimeException("Invalid token"))
                .when(userService).validatePasswordResetRequest(eq(invalidTokenDTO), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(invalidTokenDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void resetPassword_InternalServerError_EmailSent() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        doThrow(new RuntimeException("Unexpected error"))
                .when(userService).validatePasswordResetRequest(eq(validResetPasswordDTO), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(validResetPasswordDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));

        verify(emailService).sendError(any(RuntimeException.class), any(HttpServletRequest.class),
                eq(Optional.of(objectMapper.convertValue(validResetPasswordDTO, Map.class))));

    }

    @Test
    @WithMockUser(username = "testuser")
    void activateAccount_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        mockMvc.perform(post("/api/auth/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(validActivateAccountDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account is activated, you can log in now."));
    }

    @Test
    @WithMockUser(username = "testuser")
    void activateAccount_InvalidEmailFormat() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        ActivateAccountDTO invalidActivateAccountDTO = new ActivateAccountDTO();
        invalidActivateAccountDTO.setEmail("invalid-email");
        invalidActivateAccountDTO.setActivateAccountToken("");

        mockMvc.perform(post("/api/auth/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(invalidActivateAccountDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email should be valid")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void activateAccount_InvalidToken() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        ActivateAccountDTO invalidTokenDTO = new ActivateAccountDTO();
        invalidTokenDTO.setEmail("user@example.com");
        invalidTokenDTO.setActivateAccountToken("invalidToken");
        invalidTokenDTO.setPassword("newStrongPassword123!");
        invalidTokenDTO.setPasswordConfirmation("newStrongPassword123!");

        doThrow(new RuntimeException("Invalid token"))
                .when(userService).validateActivateAccountRequest(eq(invalidTokenDTO), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(invalidTokenDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUsers_FilterByStatus_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";
        String accountStatus = "ACTIVE";

        when(userService.getUsersByFilters(accountStatus, "", ""))
                .thenReturn(List.of(mockUsers.get(0)));

        mockMvc.perform(get("/api/users")
                        .param("accountStatus", accountStatus)
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUsers_FilterByName_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";
        String name = "Jane";

        when(userService.getUsersByFilters("INACTIVE", name, ""))
                .thenReturn(List.of(mockUsers.get(1)));

        mockMvc.perform(get("/api/users")
                        .param("accountStatus", "INACTIVE")
                        .param("name", name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].email").value("jane.smith@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUsers_FilterByEmail_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";
        String email = "john.doe@example.com";

        when(userService.getUsersByFilters("ACTIVE", "", email))
                .thenReturn(List.of(mockUsers.get(0)));

        mockMvc.perform(get("/api/users")
                        .param("accountStatus", "ACTIVE")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUsers_InvalidStatus_BadRequest() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        mockMvc.perform(get("/api/users")
                        .param("accountStatus", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUsers_EmptyResult_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        when(userService.getUsersByFilters("INACTIVE", "", ""))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/users")
                        .param("accountStatus", "INACTIVE")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUser_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john.doe@example.com");

        when(userService.createUser(any(UserDTO.class), any(HttpServletRequest.class)))
                .thenReturn(userDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Successfully created user JOHN DOE"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUser_InvalidEmail_BadRequest() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUser_ServerError_InternalServerError() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john.doe@example.com");

        when(userService.createUser(any(UserDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error creating user"))
                .andExpect(jsonPath("$.error").value("Database error"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUser_MissingRequiredFields_BadRequest() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("john.doe@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name cannot be blank"))
                .andExpect(jsonPath("$.lastName").value("Last name cannot be blank"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUser_Success() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john.doe@example.com");

        when(userService.getUserById(userId)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUser_NotFound() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUser_InternalServerError() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Database error"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUser_InvalidIdFormat_BadRequest() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        String invalidUserId = "abc";

        mockMvc.perform(get("/api/users/{id}", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_Unauthorized_Forbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateUser_Success() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        UserDTO updatedUser = new UserDTO();
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setEmail("john.doe@example.com");
        updatedUser.setStatus("ACTIVE");

        when(userService.updateUser(eq(userId), any(UserDTO.class), any(MockHttpServletRequest.class)))
                .thenReturn(updatedUser);

        UserDTO mockUserDTO = new UserDTO();
        mockUserDTO.setFirstName("John");
        mockUserDTO.setLastName("Doe");
        mockUserDTO.setEmail("john.doe@example.com");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(mockUserDTO)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Successfully updated user JOHN DOE"));

        verify(userService, times(1)).updateUser(eq(userId), eq(mockUserDTO), any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateUser_NotFound() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        when(userService.updateUser(eq(userId), any(UserDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Error updating user"))
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(emailService, times(1)).sendError(any(RuntimeException.class), any(HttpServletRequest.class), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateUser_InternalServerError() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        when(userService.updateUser(eq(userId), any(UserDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Error updating user"))
                .andExpect(jsonPath("$.error").value("Database error"));

        verify(emailService, times(1)).sendError(any(RuntimeException.class), any(HttpServletRequest.class), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void resendActivationEmail_Success() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        doNothing().when(userService).resendActivationEmail(eq(userId), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/users/{id}/resend-account-activation-email", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf())) // Dodajte CSRF ako je potreban
                .andExpect(status().isOk())
                .andExpect(content().string("Email send successfully"));

        verify(userService, times(1)).resendActivationEmail(eq(userId), any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void resendActivationEmail_InternalServerError() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        doThrow(new RuntimeException("Error sending email")).when(userService).resendActivationEmail(eq(userId), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/users/{id}/resend-account-activation-email", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error sending email"));

        verify(userService, times(1)).resendActivationEmail(eq(userId), any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void setState_Success() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        doNothing().when(userService).changeUserStatus(eq(userId), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/users/{id}/set-account-state", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Account status changed successfully"));

        verify(userService, times(1)).changeUserStatus(eq(userId), any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void setState_InternalServerError() throws Exception {
        String jwtToken = "Bearer mockedJwToken";

        Long userId = 1L;

        doThrow(new RuntimeException("Error changing status")).when(userService).changeUserStatus(eq(userId), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/users/{id}/set-account-state", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error changing status"));

        verify(userService, times(1)).changeUserStatus(eq(userId), any(HttpServletRequest.class));
    }
}
