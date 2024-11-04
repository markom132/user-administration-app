package com.user_admin.app.controllers;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.repository.RequestResponseLogRepository;
import com.user_admin.app.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthTokenService authTokenService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RequestResponseLogRepository requestResponseLogRepository;

    @MockBean
    private HttpServletRequest httpRequest;


    @Test
    @WithMockUser(username = "testuser")
    void testGetSessionTimeout() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        when(authTokenService.getSessionTimeout(any())).thenReturn("15");

        mockMvc.perform(get("/api/session-timeout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("15"))
                .andExpect(jsonPath("$").value("15"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateSessionTimeout_Success() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        int newTimeout = 20;
        String requestBody = "{\"sessionTimeout\": " + newTimeout + "}";

        mockMvc.perform(put("/api/session-timeout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session timeout updated successfully"))
                .andExpect(jsonPath("$.sessionTimeout").value(newTimeout));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateSessionTimeout_InvalidTimeout() throws Exception {
        String jwtToken = "Bearer mockedJwtToken";

        String requestBody = "{\"sessionTimeout\": -5}";

        mockMvc.perform(put("/api/session-timeout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid session timeout value"));
    }

}
