package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for logging request and response details.
 * This class captures information related to an HTTP request and its corresponding response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestResponseLogDTO {

    /**
     * The HTTP method of the request (e.g., GET, POST, PUT, DELETE).
     */
    private String method;

    /**
     * The endpoint URL that was called.
     */
    private String endpoint;

    /**
     * The body of the request sent to the server.
     */
    private String requestBody;

    /**
     * The body of the response received from the server.
     */
    private String responseBody;

    /**
     * The HTTP status code returned by the server.
     */
    private int statusCode;

    /**
     * The timestamp when the request was made.
     */
    private LocalDateTime timestamp;

    /**
     * The time taken to process the request, in milliseconds.
     */
    private Long executionTime;

}
