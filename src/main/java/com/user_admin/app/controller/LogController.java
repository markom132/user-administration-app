package com.user_admin.app.controller;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<List<RequestResponseLog>> getAllLogs() {
        return ResponseEntity.status(HttpStatus.OK).body(logService.getAllLogs());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<RequestResponseLog>> getLogsByCriteria(
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Integer statusCode) {

        List<RequestResponseLog> logs = logService.findByCriteria(endpoint, method, statusCode);
        return ResponseEntity.status(HttpStatus.OK).body(logs);
    }

}
