package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for handling password reset tokens.
 * It contains information about the token, its creation time, expiration time, and associated user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetTokenDTO {

    /**
     * Unique identifier for the password reset token.
     */
    private Long id;

    /**
     * The actual token string used for password reset.
     */
    private String token;

    /**
     * The timestamp when the token was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the token expires.
     */
    private LocalDateTime expiresAt;

    /**
     * THe ID of the user associated with the password reset token.
     */
    private Long userId;

}
