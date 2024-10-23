package com.user_admin.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    public interface BasicInfo {}
    public interface ExtendedInfo extends BasicInfo {}

    @JsonView(BasicInfo.class)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 255, message = "First name must have between 3 and 255 characters")
    @JsonView(BasicInfo.class)
    private String firstName;

    @NotBlank
    @Size(min = 3, max = 255, message = "Last name must have between 3 and 255 characters")
    @JsonView(BasicInfo.class)
    private String lastName;

    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    @Email
    @JsonView(BasicInfo.class)
    private String email;

    @JsonIgnore
    @JsonView(BasicInfo.class)
    private String status;

    @JsonIgnore
    private List<AuthTokenDTO> authTokens;

    @JsonIgnore
    private List<PasswordResetTokenDTO> passwordResetTokens;

    @JsonView(ExtendedInfo.class)
    private String createdAt;

    @JsonView(ExtendedInfo.class)
    private String updatedAt;

}
