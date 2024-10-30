package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for error details, encapsulating error-specific
 * information for logging or debugging purposes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {

    /**
     * Timestamp indicating when the error occurred, formatted as a string.
     */
    private String createdAt;

    /**
     * Email of the user associated with the request that caused the error, if applicable.
     */
    private String userEmail;

    /**
     * The API endpoint involved in the error.
     */
    private String api;

    /**
     * Details of the request that led to the error.
     */
    private String request;

    /**
     * Message describing the error or exception that occurred.
     */
    private String message;

    /**
     * Code line or location in the source code where the error occurred.
     */
    private String codeLine;

    /**
     * Map of parameters or data entries involved in the request, used for debugging.
     */
    private Map<String, Object> entryParams;

}
