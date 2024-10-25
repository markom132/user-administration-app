package com.user_admin.app.controller;

import com.user_admin.app.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    public AuthTokenController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @GetMapping("/session-timeout")
    public String getSessionTimeout(HttpServletRequest request) {
       return authTokenService.getSessionTimeout(request);
    }

}
