package com.user_admin.app.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for resetting a user's password.
 * This class encapsulates the information required to reset a password.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResetPasswordDTO {

    /**
     * The token used for password reset verification.
     * This token must be valid and non-empty.
     */
    @NotBlank(message = "Token is required")
    private String token;

    /**
     * The email address of the user requesting a password reset.
     * This must be a valid email format and non-empty.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    private String email;

    /**
     * The new password to be set for the user.
     * This must be at least 8 characters long and non-empty.
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    /**
     * The confirmation password that must match the new password.
     * This field is required and must be non-empty.
     */
    @NotBlank(message = "Confirmation password is required")
    private String confirmationPassword;

}
