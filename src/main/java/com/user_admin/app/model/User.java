package com.user_admin.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity class representing a user in the system.
 * This class is mapped to the 'user' table in the database.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The first name of the user.
     */
    private String firstName;

    /**
     * The last name of the user.
     */
    private String lastName;

    /**
     * The email address of the user, must not be null and has a maximum length of 255 characters.
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * The password of the user.
     */
    private String password;

    /**
     * The status of the user, represented as a enumerated type.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The timestamp when the user was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the user was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * List of authentication tokens associated with the user.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthToken> authTokens;

    /**
     * List of password reset tokens associated with the user.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordResetToken> passwordResetTokens;

    /**
     * Constructor for creating a user with email, password, first name, last name, and status.
     *
     * @param email     the email of the user
     * @param password  the password of the user
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @param status    the status of the user
     */
    public User(String email, String password, String firstName, String lastName, UserStatus status) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
    }

    /**
     * Method called before the user is persisted to set the created and updated timestamp.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Method called before the user is updated to set the updated timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
