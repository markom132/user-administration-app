package com.user_admin.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a password reset token.
 * This class is mapped to the 'password_reset_token' table in the database.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {

    /**
     * Unique identifier for the password reset token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The token string used for resetting the password.
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
     * The user associated with this password reset token.
     * This field establishes a many-to-one relationship with the User entity.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The token string used for activating an account.
     */
    private String activateAccountToken;
}
