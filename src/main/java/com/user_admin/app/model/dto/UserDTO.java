package com.user_admin.app.model.dto;

import jakarta.validation.constraints.Email;
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

    private String firstName;

    private String lastName;

    @Size(min = 3, max = 255)
    @Email
    private String email;

    private String status;

    private List<AuthTokenDTO> authTokens;

    private List<PasswordResetTokenDTO> passwordResetTokens;

}
