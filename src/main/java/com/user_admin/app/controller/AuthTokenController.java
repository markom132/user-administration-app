package com.user_admin.app.controller;

import com.user_admin.app.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    public AuthTokenController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @GetMapping("/session-timeout")
    public ResponseEntity<String> getSessionTimeout(HttpServletRequest request) {
       return ResponseEntity.status(HttpStatus.OK).body(authTokenService.getSessionTimeout(request));
    }

    @PutMapping("/session-timeout")
    public ResponseEntity<Map<String, Object>> updateSessionTimeout(@RequestBody Map<String, Integer> request,
                                                                    HttpServletRequest httpRequest) {
        Integer sessionTimeout = request.get("sessionTimeout");

        if (sessionTimeout == null || sessionTimeout <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid session timeout value"));
        }
        authTokenService.updateSessionTimeout(sessionTimeout, httpRequest);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Session timeout updated successfully", "sessionTimeout", sessionTimeout));
    }

}
