package com.user_admin.app.controller;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import com.user_admin.app.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing log-related operations.
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Retrieves all logs from the log service.
     *
     * @return ResponseEntity containing a list of RequestResponseLogDTO objects and an HTTP status
     */
    @GetMapping
    public ResponseEntity<List<RequestResponseLogDTO>> getAllLogs() {
        logger.info("Fetching all logs");

        List<RequestResponseLogDTO> logs = logService.getAllLogs();

        // Log the number of logs retrieved
        logger.info("Retrieved {} logs", logs.size());

        return ResponseEntity.status(HttpStatus.OK).body(logs);
    }

    /**
     * Retrieves logs filtered by specified criteria.
     *
     * @param endpoint   the endpoint to filter logs by (optional)
     * @param method     the HTTP method to filter logs by (optional)
     * @param statusCode the HTTP status code to filter logs by (optional)
     * @return ResponseEntity containing a list of RequestResponseLogDTO objects and an HTTP status
     */
    @GetMapping("/filter")
    public ResponseEntity<List<RequestResponseLogDTO>> getLogsByCriteria(
            @RequestParam(required = false, defaultValue = "") String endpoint,
            @RequestParam(required = false, defaultValue = "") String method,
            @RequestParam(required = false, defaultValue = "") Integer statusCode) {

        logger.info("Fetching logs with criteria - Endpoint: {}, Method: {}, Status Code: {}", endpoint, method, statusCode);

        List<RequestResponseLogDTO> logs = logService.findByCriteria(endpoint, method, statusCode);

        // Log the number of logs retrieved based on the criteria
        logger.info("Retrieved {} logs matching the criteria", logs.size());

        return ResponseEntity.status(HttpStatus.OK).body(logs);
    }

    /**
     * Retrieves logs within a specified timestamp range.
     *
     * @param start the start timestamp in a valid date-time format (e.g., "yyyy-MM-dd'T'HH:mm:ss")
     * @param end   the end timestamp in a valid date-time format (e.g., "yyyy-MM-dd'T'HH:mm:ss")
     * @return ResponseEntity containing a list of RequestResponseLogDTO objects and an HTTP status
     */
    @GetMapping("/filter/between")
    public ResponseEntity<List<RequestResponseLogDTO>> getLogsByTimestamp(@RequestParam String start, @RequestParam String end) {

        logger.info("Fetching logs between timestamps - Start: {}, End: {}", start, end);

        // Retrieves logs based on the timestamp range
        List<RequestResponseLogDTO> logs = logService.getLogsByTimestamp(start, end);

        // Log the number of logs retrieved within the specified range
        logger.info("Retrieved {} logs between the specified timestamps", logs.size());

        return ResponseEntity.status(HttpStatus.OK).body(logs);
    }
}
