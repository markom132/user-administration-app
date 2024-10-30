package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for an authentication token, which holds
 * information about token's lifecycle and association with a user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenDTO {

    /**
     * Unique identifier of the authentication token.
     */
    private Long id;

    /**
     * The JWT token string used for authentication.
     */
    private String token;

    /**
     * Timestamp indicating when the token was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating when the last time the token was used.
     */
    private LocalDateTime lastUsedAt;

    /**
     * Timestamp indicating the expiration time of the token.
     */
    private LocalDateTime expiresAt;

    /**
     * ID of the user associated with this token.
     */
    private Long userId;

}
