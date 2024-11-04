package com.user_admin.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.repository.RequestResponseLogRepository;
import com.user_admin.app.repository.UserChangeLogRepository;
import com.user_admin.app.service.UserChangeLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserChangeLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserChangeLogService userChangeLogService;

    @MockBean
    private UserChangeLogRepository userChangeLogRepository;

    @MockBean
    private RequestResponseLogRepository requestResponseLogRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private List<UserChangeLogDTO> changeLogs;

    @BeforeEach
    void setUp() {
        changeLogs = new ArrayList<>();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserChangeLog_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        Long userId = 1L;
        UserChangeLogDTO changeLog = new UserChangeLogDTO();
        changeLog.setUserId(userId);
        changeLog.setFieldName("email");
        changeLog.setOldValue("old@example.com");
        changeLog.setNewValue("new@example.com");
        changeLog.setChangedAt("2024-11-01T12:00:00");
        changeLog.setChangedByFirstName("Admin");
        changeLog.setChangedByLastName("User");

        changeLogs.add(changeLog);

        when(userChangeLogService.getUserChanges(userId)).thenReturn(changeLogs);

        mockMvc.perform(get("/api/users/{userId}/changelog", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].fieldName").value("email"))
                .andExpect(jsonPath("$[0].oldValue").value("old@example.com"))
                .andExpect(jsonPath("$[0].newValue").value("new@example.com"))
                .andExpect(jsonPath("$[0].changedAt").value("2024-11-01T12:00:00"))
                .andExpect(jsonPath("$[0].changedByFirstName").value("Admin"))
                .andExpect(jsonPath("$[0].changedByLastName").value("User"));

        verify(userChangeLogService, times(1)).getUserChanges(userId);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserChangeLog_NoChangesFound() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        Long userId = 1L;

        when(userChangeLogService.getUserChanges(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/users/{userId}/changelog", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(userChangeLogService, times(1)).getUserChanges(userId);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserChangeLog_UserNotFound() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        Long userId = 1L;

        when(userChangeLogService.getUserChanges(userId)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{userId}/changelog", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));

        verify(userChangeLogService, times(1)).getUserChanges(userId);
    }

}
