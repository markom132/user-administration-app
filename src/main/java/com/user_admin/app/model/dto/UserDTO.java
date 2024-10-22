package com.user_admin.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank
    @Size(min = 3, max = 255, message = "First name must have between 3 and 255 characters")
    private String firstName;

    @NotBlank
    @Size(min = 3, max = 255, message = "Last name must have between 3 and 255 characters")
    private String lastName;

    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    @Email
    private String email;

    @JsonIgnore
    private String status;

    @JsonIgnore
    private List<AuthTokenDTO> authTokens;

    @JsonIgnore
    private List<PasswordResetTokenDTO> passwordResetTokens;

}
