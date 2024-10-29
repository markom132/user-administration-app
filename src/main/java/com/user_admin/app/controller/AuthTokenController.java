package com.user_admin.app.controller;

import com.user_admin.app.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing authentication tokens.
 * Provides endpoints for token-related operations.
 */
@RestController
@RequestMapping("/api")
public class AuthTokenController {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenController.class);
    private final AuthTokenService authTokenService;

    /**
     * Constructor for injecting the AuthenticationService dependency.
     *
     * @param authTokenService the service to manage authentication tokens
     */
    public AuthTokenController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    /**
     * Endpoint to retrieve the duration in minutes since the user session (JWT token) was created.
     *
     * @param request to HttpServletRequest object to extract session details
     * @return ResponseEntity containing the duration in minutes as a string.
     */
    @GetMapping("/session-timeout")
    public ResponseEntity<String> getSessionTimeout(HttpServletRequest request) {
        // Log entering the session timeout calculation
        logger.info("Calculating session duration since token creation.");

        // Retrieve and log the duration since token activation
        String sessionDuration = authTokenService.getSessionTimeout(request);
        logger.info("Session active duration: {}", sessionDuration);

        // Return the session active time in response
        return ResponseEntity.status(HttpStatus.OK).body(sessionDuration);
    }

    /**
     * Updates the session timeout value based on the request payload.
     *
     * @param request     A map containing the session timeout value in minutes.
     * @param httpRequest The HTTP request containing the user's JWT token for identification.
     * @return ResponseEntity containing the updated session timeout and a success message,
     * or a message indicating a bad request if the provided timeout is invalid.
     */
    @PutMapping("/session-timeout")
    public ResponseEntity<Map<String, Object>> updateSessionTimeout(@RequestBody Map<String, Integer> request,
                                                                    HttpServletRequest httpRequest) {
        // Extract the session timeout value from the request body
        Integer sessionTimeout = request.get("sessionTimeout");

        // Validate the session timeout value
        if (sessionTimeout == null || sessionTimeout <= 0) {
            logger.warn("Invalid session timeout value provided: {}", sessionTimeout);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid session timeout value"));
        }

        // Log the update operation
        logger.info("Updating session timeout to {} minutes", sessionTimeout);

        // Update the session timeout using the authTokenService
        authTokenService.updateSessionTimeout(sessionTimeout, httpRequest);

        // Log successful update
        logger.info("Session timeout successfully updated to {} minutes", sessionTimeout);

        // Return the success message and updated session timeout value
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Session timeout updated successfully", "sessionTimeout", sessionTimeout));
    }

}
