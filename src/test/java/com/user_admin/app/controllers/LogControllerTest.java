package com.user_admin.app.controllers;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.controller.PasswordController;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import com.user_admin.app.repository.RequestResponseLogRepository;
import com.user_admin.app.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RequestResponseLogRepository requestResponseLogRepository;

    @Test
    @WithMockUser(username = "testuser")
    public void getAllLogs_ReturnsLogs_WhenLogsAreAvailable() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        RequestResponseLogDTO log1 = new RequestResponseLogDTO();
        log1.setEndpoint("/api/users");
        log1.setMethod("POST");
        log1.setStatusCode(201);
        log1.setResponseBody("testResponseBody1");
        log1.setTimestamp(LocalDateTime.now());
        log1.setExecutionTime(320L);
        log1.setRequestBody("testRequestBody1");

        RequestResponseLogDTO log2 = new RequestResponseLogDTO();
        log2.setEndpoint("/api/users");
        log2.setMethod("GET");
        log2.setStatusCode(200);
        log2.setResponseBody("testResponseBody2");
        log2.setTimestamp(LocalDateTime.now().plusHours(1));
        log2.setExecutionTime(500L);
        log2.setRequestBody("testRequestBody2");

        List<RequestResponseLogDTO> logs = Arrays.asList(log1, log2);

        when(logService.getAllLogs()).thenReturn(logs);

        mockMvc.perform(get("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].endpoint").value("/api/users"))
                .andExpect(jsonPath("$[0].statusCode").value(201))
                .andExpect(jsonPath("$[1].responseBody").value("testResponseBody2"))
                .andExpect(jsonPath("$[1].executionTime").value(500L))
        ;

        verify(logService, times(1)).getAllLogs();
    }

    @Test
    @WithMockUser(username = "testuser")
    public void getAllLogs_ReturnsEmptyList_WhenNoLogsAvailable() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        when(logService.getAllLogs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(logService, times(1)).getAllLogs();
    }

    @Test
    @WithMockUser(username = "testuser")
    public void getAllLogs_LogsServiceThrowsException_ReturnsInternalServerError() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        when(logService.getAllLogs()).thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isInternalServerError());

        verify(logService, times(1)).getAllLogs();
    }
}
