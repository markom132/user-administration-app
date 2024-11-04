package com.user_admin.app.controller;

import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.service.UserChangeLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * REST controller for handling user changeLog requests
 */
@RestController
@RequestMapping("/api")
public class UserChangeLogController {

    private static final Logger logger = LoggerFactory.getLogger(UserChangeLogController.class);
    private final UserChangeLogService userChangeLogService;

    public UserChangeLogController(UserChangeLogService userChangeLogService) {
        this.userChangeLogService = userChangeLogService;
    }

    /**
     * Retrieves the change log for a specific user.
     *
     * @param userId the ID of the user whose changeLog is going to be retrieved
     * @return a ResponseEntity containing a list of UserChangeLogDTO objects and HTTP status OK
     */
    @GetMapping("/users/{userId}/changelog")
    public ResponseEntity<?> getUserChangeLog(@PathVariable Long userId) {
        logger.info("Fetching changeLog for user ID: {}", userId);
        try {
            List<UserChangeLogDTO> changeLogs = userChangeLogService.getUserChanges(userId);
            return ResponseEntity.status(HttpStatus.OK).body(changeLogs);
        } catch (ResourceNotFoundException e) {
            logger.error("Error retrieving change log: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonList(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error retrieving change log for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList("Internal server error"));
        }
    }

}
