package com.user_admin.app.controller;

import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.service.UserChangeLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserChangeLogController {

    private final UserChangeLogService userChangeLogService;

    public UserChangeLogController(UserChangeLogService userChangeLogService) {
        this.userChangeLogService = userChangeLogService;
    }

    @GetMapping("/users/{userId}/changelog")
    public ResponseEntity<List<UserChangeLogDTO>> getUserChangeLog(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userChangeLogService.getUserChanges(userId));
    }

}
