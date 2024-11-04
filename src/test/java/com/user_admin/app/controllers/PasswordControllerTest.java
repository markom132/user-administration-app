package com.user_admin.app.controllers;

import com.user_admin.app.controller.PasswordController;
import com.user_admin.app.repository.RequestResponseLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestResponseLogRepository requestResponseLogRepository;

    @Test
    @WithMockUser(username = "testuser")
    void showResetPasswordPage_ReturnsResetPasswordView() throws Exception {
        String token = "resetToken";
        String email = "user@example.com";
        String jwtToken = "jwtToken";

        mockMvc.perform(get("/api/reset-password/{token}/{email}/{jwtToken}", token, email, jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("reset_password"))
                .andExpect(model().attributeExists("token"))
                .andExpect(model().attribute("token", token))
                .andExpect(model().attributeExists("email"))
                .andExpect(model().attribute("email", email))
                .andExpect(model().attributeExists("jwtToken"))
                .andExpect(model().attribute("jwtToken", jwtToken));
    }

    @Test
    @WithMockUser(username = "testuser")
    void showActivateAccountPage_ReturnsActivateAccountView() throws Exception {
        String activationToken = "activationToken";
        String email = "user@example.com";
        String jwtToken = "jwtToken";

        mockMvc.perform(get("/api/activate-account/{activationToken}/{email}/{jwtToken}", activationToken, email, jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("activate_account"))
                .andExpect(model().attributeExists("activationToken"))
                .andExpect(model().attribute("activationToken", activationToken))
                .andExpect(model().attributeExists("email"))
                .andExpect(model().attribute("email", email))
                .andExpect(model().attributeExists("jwtToken"))
                .andExpect(model().attribute("jwtToken", jwtToken));
    }
}
