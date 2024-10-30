package com.user_admin.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a log of HTTP requests and responses.
 * This class is mapped to the 'request_response_log' table in the database.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestResponseLog {

    /**
     * Unique identifier for the request-response log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The HTTP method of the request (e.g., GET, POST).
     */
    private String method;

    /**
     * The endpoint that was accessed.
     */
    private String endpoint;

    /**
     * The body of the request.
     */
    private String requestBody;

    /**
     * The body of the response, limited to 500 characters.
     */
    @Column(length = 500)
    private String responseBody;

    /**
     * The HTTP status code returned by the response.
     */
    private int statusCode;

    /**
     * The timestamp when the request was received.
     */
    private LocalDateTime timestamp;

    /**
     * The timestamp when the response was sent.
     */
    private LocalDateTime responseTimestamp;

    /**
     * The duration of time (in milliseconds) it took to proceed the request.
     */
    private Long executionTime;
}
