package com.user_admin.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a changeLog for user data.
 * This class is mapped to the 'user_change_log' table in the database.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChangeLog {

    /**
     * Unique identifier for the change log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user associated with this change log entry.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The name of the field that was changed.
     */
    private String fieldName;

    /**
     * The old value of the field before the change.
     */
    private String oldValue;

    /**
     * The new value of the field after the change.
     */
    private String newValue;

    /**
     * The timestamp when the change occurred.
     */
    private LocalDateTime changedAt;

    /**
     * The first name of the user who made the change.
     */
    private String changedByFirstName;

    /**
     * The last name of the user who made the change.
     */
    private String changedByLastName;
}
