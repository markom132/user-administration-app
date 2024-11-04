package com.user_admin.app.services;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.User;
import com.user_admin.app.model.UserChangeLog;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.model.dto.mappers.UserChangeLogMapper;
import com.user_admin.app.repository.UserChangeLogRepository;
import com.user_admin.app.repository.UserRepository;
import com.user_admin.app.service.UserChangeLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserChangeLogServiceTest {

    @Mock
    private UserChangeLogRepository userChangeLogRepository;

    @Mock
    private UserChangeLogMapper userChangeLogMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    private UserChangeLogService userChangeLogService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userChangeLogService = new UserChangeLogService(userChangeLogRepository, userChangeLogMapper, jwtUtil, userRepository);
    }

    @Test
    public void testGetUserChanges_WhenChangesExist() {
        Long userId = 1L;
        UserChangeLog changeLog1 = new UserChangeLog();
        UserChangeLog changeLog2 = new UserChangeLog();
        List<UserChangeLog> changeLogs = List.of(changeLog1, changeLog2);
        List<UserChangeLogDTO> changeLogDTOs = List.of(new UserChangeLogDTO(), new UserChangeLogDTO());

        when(userChangeLogRepository.findByUserId(userId)).thenReturn(Optional.of(changeLogs));
        when(userChangeLogMapper.toDtoList(changeLogs)).thenReturn(changeLogDTOs);

        List<UserChangeLogDTO> result = userChangeLogService.getUserChanges(userId);

        assertEquals(2, result.size());
        verify(userChangeLogRepository).findByUserId(userId);
        verify(userChangeLogMapper).toDtoList(changeLogs);
    }

    @Test
    public void testGetUserChanges_WhenNoChangesExist() {
        Long userId = 1L;

        when(userChangeLogRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userChangeLogService.getUserChanges(userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());

        verify(userChangeLogRepository).findByUserId(userId);
        verify(userChangeLogMapper, never()).toDtoList(anyList());
    }

    @Test
    public void testGetUserChanges_WhenChangeLogIsEmpty() {
        Long userId = 1L;

        when(userChangeLogRepository.findByUserId(userId)).thenReturn(Optional.of(Collections.emptyList()));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userChangeLogService.getUserChanges(userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());

        verify(userChangeLogRepository).findByUserId(userId);
        verify(userChangeLogMapper, never()).toDtoList(anyList());
    }

    @Test
    public void testLogChange_Success() {
        UserChangeLog changeLog = new UserChangeLog();
        User user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");

        String token = "Bearer valid.token.here";
        String changedByEmail = "changedBy@example.com";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername("valid.token.here")).thenReturn(changedByEmail);

        User changedByUser = new User();
        changedByUser.setFirstName("Changed");
        changedByUser.setLastName("User");

        when(userRepository.findByEmail(changedByEmail)).thenReturn(Optional.of(changedByUser));

        userChangeLogService.logChange(changeLog, user, request);

        assertEquals(user, changeLog.getUser());
        assertEquals("Changed", changeLog.getChangedByFirstName());
        assertEquals("User", changeLog.getChangedByLastName());

        verify(userChangeLogRepository).save(changeLog);
        verify(userRepository).findByEmail(changedByEmail);
    }

    @Test
    public void testLogChange_UserNotFound() {
        UserChangeLog changeLog = new UserChangeLog();
        User user = new User();
        String token = "Bearer valid.token.here";
        String changedByEmail = "changedBy@example.com";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername("valid.token.here")).thenReturn(changedByEmail);
        when(userRepository.findByEmail(changedByEmail)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userChangeLogService.logChange(changeLog, user, request));

        assertEquals("User with email: " + changedByEmail + " not found", exception.getMessage());
        verify(userChangeLogRepository, never()).save(any());
    }

    @Test
    public void testFillUserChangeLogDTO() {
        String fieldName = "username";
        String oldValue = "oldUsername";
        String newValue = "newUsername";

        UserChangeLog changeLog = userChangeLogService.fillUserChangeLogDTO(fieldName, oldValue, newValue);

        assertNotNull(changeLog);
        assertEquals(fieldName, changeLog.getFieldName());
        assertEquals(oldValue, changeLog.getOldValue());
        assertEquals(newValue, changeLog.getNewValue());
        assertNotNull(changeLog.getChangedAt());
    }

    @Test
    public void testFillUserChangeLogDTO_NullValues() {
        String fieldName = null;
        String oldValue = null;
        String newValue = null;

        UserChangeLog changeLog = userChangeLogService.fillUserChangeLogDTO(fieldName, oldValue, newValue);

        assertNotNull(changeLog);
        assertNull(changeLog.getFieldName());
        assertNull(changeLog.getOldValue());
        assertNull(changeLog.getNewValue());
        assertNotNull(changeLog.getChangedAt());
    }
}
