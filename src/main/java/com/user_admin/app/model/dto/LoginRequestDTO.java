package com.user_admin.app.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling login requests. Includes user email and password validation.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequestDTO {

    /**
     * User's email address for login. Must be valid and within character limits.
     */
    @NotBlank(message = "Email cannot be blank")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * User's password for login. Must meet minimum length requirements.
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

}
