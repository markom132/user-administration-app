package com.user_admin.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for representing user information.
 * This class is used to transfer user data between different layers of the application.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    /**
     * Interface for basic user information view.
     */
    public interface BasicInfo {
    }

    /**
     * Interface for extended user information view, which includes basic info.
     */
    public interface ExtendedInfo extends BasicInfo {
    }

    /**
     * Unique identifier for the user.
     */
    @JsonView(BasicInfo.class)
    private Long id;

    /**
     * User's first name.
     * Must be between 3 and 255 characters and cannot be blank.
     */
    @NotBlank
    @Size(min = 3, max = 255, message = "First name must have between 3 and 255 characters")
    @JsonView(BasicInfo.class)
    private String firstName;

    /**
     * User's last name.
     * Must be between 3 and 255 characters and cannot be blank.
     */
    @NotBlank
    @Size(min = 3, max = 255, message = "Last name must have between 3 and 255 characters")
    @JsonView(BasicInfo.class)
    private String lastName;

    /**
     * User's email address.
     * Must be between 3 and 255 characters and must be a valid email format.
     */
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    @Email
    @JsonView(BasicInfo.class)
    private String email;

    /**
     * User's status (e.g., ACTIVE, INACTIVE).
     * This field is ignored during serialization.
     */
    @JsonIgnore
    @JsonView(BasicInfo.class)
    private String status;

    /**
     * List of authentication tokens associated with the user.
     * This field is ignored during serialization.
     */
    @JsonIgnore
    private List<AuthTokenDTO> authTokens;

    /**
     * List of password reset tokens associated with the user.
     * This field is ignored during serialization.
     */
    @JsonIgnore
    private List<PasswordResetTokenDTO> passwordResetTokens;

    /**
     * The timestamp when a user was created.
     * This field is included in the extended user information view.
     */
    @JsonView(ExtendedInfo.class)
    private String createdAt;

    /**
     * The timestamp when the user was last updated.
     * This field is included in the extended user information view.
     */
    @JsonView(ExtendedInfo.class)
    private String updatedAt;

}
