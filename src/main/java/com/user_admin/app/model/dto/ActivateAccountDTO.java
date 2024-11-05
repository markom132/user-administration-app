package com.user_admin.app.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for activating a user account. Contains necessary fields to
 * validate and activate an account.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActivateAccountDTO {

    /**
     * Token used to verify account activation
     */
    private String activateAccountToken;

    /**
     * User email address, required and should be a valid email format.
     */
    @NotBlank(message = "Email cannot be blank")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * User password, required and must be at least 8 characters.
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Confirmation of the user's password.
     */
    private String passwordConfirmation;

}
