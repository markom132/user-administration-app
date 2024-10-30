package com.user_admin.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing an authentication token.
 * This class is mapped to the 'auth_tokens' table in the database.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthToken {

    /**
     * Unique identifier for the authentication token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The JWT token string used for authentication.
     */
    private String token;

    /**
     * The timestamp when the token was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the token was last used.
     */
    private LocalDateTime lastUsedAt;

    /**
     * The timestamp when the token expires.
     */
    private LocalDateTime expiresAt;

    /**
     * The user associated with this authentication token.
     * This field establishes a many-to-one relationship with the User entity.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
