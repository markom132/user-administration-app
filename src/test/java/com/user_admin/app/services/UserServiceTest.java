package com.user_admin.app.services;

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
import com.user_admin.app.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserChangeLogService userChangeLogService;

    private UserService userService;

    private User user;

    private AuthToken authToken;

    @Mock
    private HttpServletRequest request;

    private UserDTO userDTO;

    private UserDTO updatedData;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, jwtUtil, authenticationManager,
                passwordEncoder, authTokenService, emailService, passwordResetTokenService,
                userMapper, userChangeLogService);

        userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");

        updatedData = new UserDTO();
        updatedData.setFirstName("Jane");
        updatedData.setLastName("Smith");
        updatedData.setEmail("jane.smith@example.com");

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("$2a$10$abc123");
        user.setFirstName("JOHN");
        user.setLastName("Doe");
        user.setStatus(UserStatus.ACTIVE);

        authToken = new AuthToken();
        authToken.setToken("sampleToken");
        authToken.setExpiresAt(LocalDateTime.now().plusHours(1));

    }

    @Test
    public void testLogin_Success() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("generatedToken");
        when(authTokenService.createAuthToken(any(), any())).thenReturn(authToken);

        Map<String, Object> response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals(user.getId(), response.get("id"));
        assertEquals(user.getEmail(), response.get("email"));
        assertEquals(user.getFirstName(), response.get("firstName"));
        assertEquals(user.getLastName(), response.get("lastName"));
        assertEquals(String.valueOf(user.getStatus()), response.get("status"));
        assertEquals("generatedToken", response.get("token"));
        assertNotNull(response.get("expiresAt"));
    }

    @Test
    public void testLogin_InvalidCredentials() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "wrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    public void testLogin_UserInactive() {
        user.setStatus(UserStatus.INACTIVE);
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
        assertEquals("User account is inactive", exception.getMessage());
    }

    @Test
    public void testLogin_AuthenticationFailed() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Authentication failed") {
                });

        Exception exception = assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    public void testLogout_Success() {
        String authorizationHeader = "Bearer sampleToken";

        when(authTokenService.findByToken("sampleToken")).thenReturn(authToken);

        userService.logout(authorizationHeader);

        verify(authTokenService).updateToExpired(authToken);
    }

    @Test
    public void testLogout_TokenNotFound() {
        String authorizationHeader = "Bearer sampleToken";

        when(authTokenService.findByToken("sampleToken")).thenThrow(new ResourceNotFoundException("Token not found: sampleToken"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.logout(authorizationHeader));
        assertEquals("Token not found: sampleToken", exception.getMessage());
    }

    @Test
    public void testLogout_MissingAuthorizationHeader() {
        String authorizationHeader = "";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.logout(authorizationHeader));
        assertEquals("Invalid Authorization header format", exception.getMessage());
    }

    @Test
    void testForgotPassword_Success() throws IOException {
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);

        String hashedToken = "hashedGeneratedToken";
        String jwtToken = "mockJwtToken";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn(hashedToken);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        doNothing().when(passwordResetTokenService).createPasswordResetToken(hashedToken, user);

        // ArgumentCaptor for resetLink
        ArgumentCaptor<String> resetLinkCaptor = forClass(String.class);

        userService.forgotPassword(email, request);

        verify(passwordResetTokenService).createPasswordResetToken(hashedToken, user);
        verify(emailService).sendResetPasswordEmail(eq(email), resetLinkCaptor.capture());

        String resetLink = resetLinkCaptor.getValue();
        assertTrue(resetLink.contains(email));
        assertTrue(resetLink.contains(jwtToken));
    }

    @Test
    void testForgotPassword_UserNotFound() throws IOException {
        String email = "user@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.forgotPassword(email, request));
        assertEquals("User not found with email: " + email, exception.getMessage());

        verify(passwordResetTokenService, never()).createPasswordResetToken(anyString(), any(User.class));
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    void testForgotPassword_UserInactive() throws IOException {
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.forgotPassword(email, request));
        assertEquals("User account is not active", exception.getMessage());

        verify(passwordResetTokenService, never()).createPasswordResetToken(anyString(), any(User.class));
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    void testForgotPassword_MissingAuthorizationHeader() throws IOException {
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(request.getHeader("Authorization")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.forgotPassword(email, request));
        assertEquals("Authorization header is missing or invalid", exception.getMessage());

        verify(passwordResetTokenService, never()).createPasswordResetToken(anyString(), any(User.class));
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    void testValidatePasswordResetRequest_PasswordMismatch() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("user@example.com", "token", "NewPassword123!", "Mismatch123!");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        assertEquals("New password and confirmation password don't match", exception.getMessage());
    }

    @Test
    void testValidatePasswordResetRequest_InvalidPasswordFormat() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("user@example.com", "token", "weakpassword", "weakpassword");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        assertEquals("Password must contain at least one uppercase letter, one number, and one special character", exception.getMessage());
    }

    @Test
    void testValidatePasswordResetRequest_NoResetTokens() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("token", "user@example.com", "NewPassword123!", "NewPassword123!");
        when(passwordResetTokenService.findAllByUserEmail("user@example.com")).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        assertEquals("No reset tokens for email: user@example.com", exception.getMessage());
    }

    @Test
    void testValidatePasswordResetRequest_InvalidToken() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("invalidToken", "user@example.com", "NewPassword123!", "NewPassword123!");
        PasswordResetToken validToken = new PasswordResetToken();

        validToken.setToken("hashedValidToken");
        validToken.setCreatedAt(LocalDateTime.now());
        validToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        validToken.setUser(user);

        when(passwordResetTokenService.findAllByUserEmail("user@example.com")).thenReturn(List.of(validToken));
        when(passwordEncoder.matches("invalidToken", "hashedValidToken")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        assertEquals("Invalid reset resetToken", exception.getMessage());
    }

    @Test
    void testValidatePasswordResetRequest_UserInactive() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("token", "user@example.com", "NewPassword123!", "NewPassword123!");

        user.setStatus(UserStatus.INACTIVE);

        PasswordResetToken validToken = new PasswordResetToken();
        validToken.setToken("hashedValidToken");
        validToken.setCreatedAt(LocalDateTime.now());
        validToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        validToken.setUser(user);

        when(passwordResetTokenService.findAllByUserEmail("user@example.com")).thenReturn(List.of(validToken));
        when(passwordEncoder.matches("token", "hashedValidToken")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        assertEquals("User is inactive: user@example.com", exception.getMessage());
    }

    @Test
    void testValidatePasswordResetRequest_TokenExpired() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("token", "user@example.com", "NewPassword123!", "NewPassword123!");
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setToken("hashedValidToken");
        expiredToken.setCreatedAt(LocalDateTime.now().minusDays(1));
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setUser(user);

        when(passwordResetTokenService.findAllByUserEmail("user@example.com")).thenReturn(List.of(expiredToken));
        when(passwordEncoder.matches("token", "hashedValidToken")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        assertEquals("Reset resetToken is expired", exception.getMessage());
    }

    @Test
    void testValidatePasswordResetRequest_Success() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("token", "user@example.com", "NewPassword123!", "NewPassword123!");
        PasswordResetToken validToken = new PasswordResetToken();
        validToken.setToken("hashedValidToken");
        validToken.setCreatedAt(LocalDateTime.now());
        validToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        validToken.setUser(user);

        when(passwordResetTokenService.findAllByUserEmail("user@example.com")).thenReturn(List.of(validToken));
        when(passwordEncoder.matches("token", "hashedValidToken")).thenReturn(true);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));

        doNothing().when(passwordResetTokenService).deleteToken(validToken);

        assertDoesNotThrow(() -> userService.validatePasswordResetRequest(resetPasswordDTO, request));

        verify(passwordResetTokenService).deleteToken(validToken);
    }

    @Test
    void testIsValidPassword_ValidPassword() {
        String password = "ValidPass123!";
        assertTrue(userService.isValidPassword(password), "Password should be valid");
    }

    @Test
    void testIsValidPassword_InvalidPassword_NoUpperCase() {
        String password = "invalidpass123!";
        assertFalse(userService.isValidPassword(password), "Password without uppercase letters should be invalid");
    }

    @Test
    void testIsValidPassword_InvalidPassword_NoLowerCase() {
        String password = "INVALID123!";
        assertFalse(userService.isValidPassword(password), "Password without lowercase letters should be invalid");
    }

    @Test
    void testIsValidPassword_InvalidPassword_NoDigit() {
        String password = "InvalidPass!";
        assertFalse(userService.isValidPassword(password), "Password without numbers should be invalid");
    }

    @Test
    void testIsValidPassword_InvalidPassword_NoSpecialCharacter() {
        String password = "InvalidPass123";
        assertFalse(userService.isValidPassword(password), "Password without special character should be invalid");
    }

    @Test
    void testIsValidPassword_InvalidPassword_ShortLength() {
        String password = "Inv1!";
        assertFalse(userService.isValidPassword(password), "Password should have at least 8 characters");
    }

    @Test
    void testIsValidPassword_InvalidPassword_Empty() {
        String password = "";
        assertFalse(userService.isValidPassword(password), "Password can't be blank");
    }

    @Test
    void testIsValidPassword_InvalidPassword_Null() {
        String password = null;
        assertFalse(userService.isValidPassword(password), "Password can't be null");
    }

    @Test
    void testValidateActivateAccountRequest_Success() {
        ActivateAccountDTO dto = new ActivateAccountDTO("validToken", "user@example.com", "Password123!", "Password123!");
        HttpServletRequest request = mock(HttpServletRequest.class);

        user.setEmail("user@example.com");
        user.setStatus(UserStatus.INACTIVE);

        PasswordResetToken validToken = new PasswordResetToken();
        validToken.setActivateAccountToken(dto.getActivateAccountToken());
        validToken.setUser(user);
        validToken.setCreatedAt(LocalDateTime.now().minusHours(1));
        validToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(passwordResetTokenService.findAllByUserEmail(user.getEmail())).thenReturn(List.of(validToken));
        when(passwordEncoder.matches(dto.getActivateAccountToken(), validToken.getActivateAccountToken())).thenReturn(true);

        assertDoesNotThrow(() -> userService.validateActivateAccountRequest(dto, request));

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(passwordResetTokenService).findAllByUserEmail("user@example.com");
    }

    @Test
    void testValidateActivateAccountRequest_UserNotFound() {
        ActivateAccountDTO dto = new ActivateAccountDTO("validToken", "user@example.com", "Password123!", "Password123!");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.validateActivateAccountRequest(dto, request));
        assertEquals("User not found with email: user@example.com", exception.getMessage());
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void testValidateActivateAccountRequest_AlreadyActive() {
        ActivateAccountDTO dto = new ActivateAccountDTO("validToken", "user@example.com", "Password123!", "Password123!");
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = new User();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.validateActivateAccountRequest(dto, request));
        assertEquals("User is already active", exception.getMessage());
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void testValidateActivateAccountRequest_PasswordMismatch() {
        ActivateAccountDTO dto = new ActivateAccountDTO("validToken", "user@example.com", "DifferentPassword", "Password123!");
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = new User();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.validateActivateAccountRequest(dto, request));
        assertEquals("New password and confirmation password don't match", exception.getMessage());
    }

    @Test
    void testValidateActivateAccountRequest_InvalidPasswordFormat() {
        ActivateAccountDTO dto = new ActivateAccountDTO("validToken", "user@example.com", "password", "password");
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = new User();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.validateActivateAccountRequest(dto, request));
        assertEquals("Password must contain at least one uppercase letter, one number, and one special character", exception.getMessage());
    }

    @Test
    void testGetUsersByFilters_AllFiltersProvided() {
        String status = "ACTIVE";
        String name = "John Doe";
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStatus(UserStatus.ACTIVE);

        List<User> users = List.of(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setStatus(String.valueOf(user.getStatus()));

        List<UserDTO> userDTOs = List.of(userDTO);

        when(userRepository.findByFilters(UserStatus.ACTIVE, "John", "Doe", email)).thenReturn(Optional.of(users));
        when(userMapper.toDtoList(users)).thenReturn(userDTOs);

        List<UserDTO> result = userService.getUsersByFilters(status, name, email);

        assertEquals(1, result.size());
        assertEquals(email, result.get(0).getEmail());
    }


    @Test
    void testGetUsersByFilters_OnlyStatusProvided() {
        String status = "ACTIVE";
        String name = "";
        String email = "";

        User user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmail("jane.doe@example.com");
        user2.setStatus(UserStatus.ACTIVE);

        List<User> users = List.of(user, user2);

        UserDTO userDTO1 = new UserDTO();
        userDTO1.setEmail(user.getEmail());
        userDTO1.setFirstName(user.getFirstName());
        userDTO1.setLastName(user.getLastName());
        userDTO1.setStatus(String.valueOf(user.getStatus()));

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setEmail(user2.getEmail());
        userDTO2.setFirstName(user2.getFirstName());
        userDTO2.setLastName(user2.getLastName());
        user2.setStatus(user2.getStatus());

        List<UserDTO> userDTOs = List.of(userDTO1, userDTO2);

        when(userRepository.findByFilters(UserStatus.ACTIVE, "", "", "")).thenReturn(Optional.of(users));
        when(userMapper.toDtoList(users)).thenReturn(userDTOs);

        List<UserDTO> result = userService.getUsersByFilters(status, name, email);

        assertEquals(2, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        assertEquals(user2.getEmail(), result.get(1).getEmail());
    }

    @Test
    void testGetUsersByFilters_InvalidStatus() {
        String status = "INVALID_STATUS";
        String name = "";
        String email = "";

        assertThrows(IllegalArgumentException.class, () -> userService.getUsersByFilters(status, name, email));
    }

    @Test
    void testGetUsersByFilters_FirstAndLastNameProvided() {
        String status = "ACTIVE";
        String name = "John Smith";
        String email = "";

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setEmail("john.smith@example.com");
        user.setStatus(UserStatus.ACTIVE);

        List<User> users = List.of(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setStatus(String.valueOf(user.getStatus()));

        List<UserDTO> userDTOs = List.of(userDTO);

        when(userRepository.findByFilters(UserStatus.ACTIVE, "John", "Smith", "")).thenReturn(Optional.of(users));
        when(userMapper.toDtoList(users)).thenReturn(userDTOs);

        List<UserDTO> result = userService.getUsersByFilters(status, name, email);

        assertEquals(1, result.size());
        assertEquals("Smith", result.get(0).getLastName());
        assertEquals("john.smith@example.com", result.get(0).getEmail());
    }


    @Test
    void testGetUsersByFilters_EmptyFilters() {
        String status = "";
        String name = "";
        String email = "";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUsersByFilters(status, name, email);
        });

        assertEquals("Status must not be empty", exception.getMessage());
    }


    @Test
    void testGetUsersByFilters_NoMatchingResults() {
        String status = "ACTIVE";
        String name = "Nonexistent";
        String email = "nonexistent@example.com";

        when(userRepository.findByFilters(UserStatus.ACTIVE, "Nonexistent", "", "nonexistent@example.com")).thenReturn(Optional.empty());
        List<UserDTO> result = userService.getUsersByFilters(status, name, email);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateUser_Success() throws IOException {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedToken");
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");


        UserDTO createdUser = userService.createUser(userDTO, request);

        assertEquals(userDTO.getEmail(), createdUser.getEmail());
        verify(userRepository).save(user);
        verify(passwordResetTokenService).createActivateAccountToken("hashedToken", user);
        verify(emailService).sendActivateAccountEmail(eq(userDTO.getEmail()), anyString());
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.createUser(userDTO, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_SendsActivationEmail() throws IOException {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedToken");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");

        userService.createUser(userDTO, request);

        verify(emailService).sendActivateAccountEmail(eq(userDTO.getEmail()), anyString());
    }

    @Test
    void testCreateUser_GeneratesActivationToken() throws IOException {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedToken");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");

        userService.createUser(userDTO, request);

        verify(passwordResetTokenService).createActivateAccountToken("hashedToken", user);
    }

    @Test
    void testGetUserById_Success() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userDTO.getId(), result.getId());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository).findById(userId);
        verify(userMapper).toDTO(user);
    }

    @Test
    void testGetUserById_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
        assertEquals("User with ID: " + userId + " not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(updatedData);

        UserDTO result = userService.updateUser(1L, updatedData, request);

        assertNotNull(result);
        assertEquals(updatedData.getEmail(), result.getEmail());
        verify(userRepository).save(user);
        verify(userMapper).toDTO(user);
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("jane.smith@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateEmailException.class, () -> userService.updateUser(1L, updatedData, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, updatedData, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_ChangeFirstName() {
        updatedData.setLastName("Doe");
        updatedData.setEmail("john.doe@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserChangeLog changeLog = new UserChangeLog();
        changeLog.setOldValue("JOHN");
        changeLog.setNewValue("JANE");
        changeLog.setFieldName("firstName");

        when(userChangeLogService.fillUserChangeLogDTO("firstName", "JOHN", "JANE")).thenReturn(changeLog);

        userService.updateUser(1L, updatedData, request);

        verify(userChangeLogService).logChange(eq(changeLog), eq(user), eq(request));
        verify(userRepository).save(user);
    }


    @Test
    void testUpdateUser_ChangeLastName() {
        updatedData.setFirstName("John");
        updatedData.setLastName("Smith");
        updatedData.setEmail("user@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserChangeLog changeLog = new UserChangeLog();
        changeLog.setOldValue("Doe");
        changeLog.setNewValue("SMITH");
        changeLog.setFieldName("lastName");

        when(userChangeLogService.fillUserChangeLogDTO("lastName", "Doe", "SMITH")).thenReturn(changeLog);

        // Act
        userService.updateUser(1L, updatedData, request);

        // Assert
        ArgumentCaptor<UserChangeLog> logCaptor = forClass(UserChangeLog.class);
        ArgumentCaptor<User> userCaptor = forClass(User.class);

        verify(userChangeLogService, times(1)).fillUserChangeLogDTO("lastName", "Doe", "SMITH");
        verify(userChangeLogService, times(1)).logChange(logCaptor.capture(), userCaptor.capture(), eq(request));
        verify(userRepository, times(1)).save(user);

        assertNotNull(logCaptor.getValue(), "logChange should not be null");
        assertEquals(changeLog.getOldValue(), logCaptor.getValue().getOldValue());
        assertEquals(changeLog.getNewValue(), logCaptor.getValue().getNewValue());
        assertEquals("lastName", logCaptor.getValue().getFieldName());
        assertEquals(user.getId(), userCaptor.getValue().getId());
    }


    @Test
    public void resendActivationEmail_UserFound_EmailSent() throws IOException {
        user.setStatus(UserStatus.INACTIVE);

        PasswordResetToken token = new PasswordResetToken();
        token.setActivateAccountToken("oldToken");
        token.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedToken");
        when(passwordResetTokenService.findAllByUserEmail(user.getEmail())).thenReturn(Arrays.asList(token));

        userService.resendActivationEmail(1L, request);

        verify(passwordResetTokenService, times(1)).deleteToken(token);
        verify(passwordResetTokenService, times(1)).createActivateAccountToken(eq("hashedToken"), eq(user));
        verify(emailService, times(1)).sendActivateAccountEmail(eq(user.getEmail()), anyString());
    }

    @Test
    public void resendActivationEmail_UserAlreadyActive_ThrowsException() {
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.resendActivationEmail(1L, request);
        });
        assertEquals("User with ID: 1 is already active", thrown.getMessage());
    }

    @Test
    public void resendActivationEmail_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.resendActivationEmail(1L, request);
        });
        assertEquals("User with ID: 1 not found", thrown.getMessage());
    }

    @Test
    public void changeUserStatus_ActiveToInactive_StatusChanged() {
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userChangeLogService.fillUserChangeLogDTO("status", "ACTIVE", "INACTIVE")).thenReturn(new UserChangeLog());

        userService.changeUserStatus(1L, request);

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userChangeLogService, times(1)).logChange(any(), eq(user), eq(request));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void changeUserStatus_InactiveToActive_StatusChanged() {
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userChangeLogService.fillUserChangeLogDTO("status", "INACTIVE", "ACTIVE")).thenReturn(new UserChangeLog());

        userService.changeUserStatus(1L, request);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userChangeLogService, times(1)).logChange(any(), eq(user), eq(request));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void changeUserStatus_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.changeUserStatus(1L, request);
        });
        assertEquals("User with ID: 1 not found", thrown.getMessage());
    }

    @Test
    public void createLink_ValidParameters_ReturnsCorrectLink() {
        String token = "sampleToken";
        String email = "user@example.com";
        String jwtToken = "jwtSampleToken";

        String result = userService.createLink(token, email, jwtToken);

        String expectedLink = "http://localhost:8080/api/activate-account/sampleToken/user@example.com/jwtSampleToken";
        assertEquals(expectedLink, result);
    }

    @Test
    public void createLink_EmptyToken_ReturnsCorrectLink() {
        String token = "";
        String email = "user@example.com";
        String jwtToken = "jwtSampleToken";

        String result = userService.createLink(token, email, jwtToken);

        String expectedLink = "http://localhost:8080/api/activate-account//user@example.com/jwtSampleToken";
        assertEquals(expectedLink, result);
    }

    @Test
    public void createLink_EmptyEmail_ReturnsCorrectLink() {
        String token = "sampleToken";
        String email = "";
        String jwtToken = "jwtSampleToken";

        String result = userService.createLink(token, email, jwtToken);

        String expectedLink = "http://localhost:8080/api/activate-account/sampleToken//jwtSampleToken";
        assertEquals(expectedLink, result);
    }

    @Test
    public void createLink_NullParameters_ReturnsCorrectLink() {
        String token = null;
        String email = null;
        String jwtToken = null;

        String result = userService.createLink(token, email, jwtToken);

        String expectedLink = "http://localhost:8080/api/activate-account/null/null/null";
        assertEquals(expectedLink, result);
    }

}
