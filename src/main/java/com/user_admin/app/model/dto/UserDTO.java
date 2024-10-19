package com.user_admin.app.model.dto;

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
    private String email;
    private String password;
    private String status;
    private List<AuthTokenDTO> authTokens;
    private List<PasswordResetTokenDTO> passwordResetTokens;

}
