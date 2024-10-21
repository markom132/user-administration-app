package com.user_admin.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private String status;

    @JsonIgnore
    private List<AuthTokenDTO> authTokens;

    @JsonIgnore
    private List<PasswordResetTokenDTO> passwordResetTokens;

}
