package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for logging changes made to user information.
 * This class captures details about the changes made to a user's attributes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChangeLogDTO {

    /**
     * The ID of the user whose information has been changed.
     */
    private Long userId;

    /**
     * The name of the field that has been changed (e.g., "email", "firstName").
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
     * The date and time when the change was made, formatted as a string.
     */
    private String changedAt;

    /**
     * The first name of the person who made the changes.
     */
    private String changedByFirstName;

    /**
     * The last name of the person who made the changes.
     */
    private String changedByLastName;

}
