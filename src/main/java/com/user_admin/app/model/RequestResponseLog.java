package com.user_admin.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestResponseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;

    private String endpoint;

    private String requestBody;

    @Column(length = 500)
    private String responseBody;

    private int statusCode;

    private LocalDateTime timestamp;

    private LocalDateTime responseTimestamp;

    private Long executionTime;
}
