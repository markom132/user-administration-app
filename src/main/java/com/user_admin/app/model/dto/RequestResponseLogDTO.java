package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestResponseLogDTO {

    private String method;
    private String endpoint;
    private String requestBody;
    private String responseBody;
    private int statusCode;
    private LocalDateTime timestamp;
    private Long executionTime;

}
