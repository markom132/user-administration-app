package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.model.dto.AuthTokenDTO;
import com.user_admin.app.model.dto.PasswordResetTokenDTO;
import com.user_admin.app.model.dto.UserDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final AuthTokenMapper authTokenMapper;
    private final PasswordResetTokenMapper passwordResetTokenMapper;

    public UserMapper(AuthTokenMapper authTokenMapper, PasswordResetTokenMapper passwordResetTokenMapper) {
        this.authTokenMapper = authTokenMapper;
        this.passwordResetTokenMapper = passwordResetTokenMapper;
    }

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        List<AuthTokenDTO> authTokens = user.getAuthTokens() != null ?
                user.getAuthTokens().stream().map(authTokenMapper::toDTO)
                        .collect(Collectors.toList()) : null;

        List<PasswordResetTokenDTO> passwordResetTokens = user.getPasswordResetTokens() != null ?
                user.getPasswordResetTokens().stream().map(passwordResetTokenMapper::toDTO)
                        .collect(Collectors.toList()) : null;

        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus().name(),
                authTokens,
                passwordResetTokens
        );
    }

    public List<UserDTO> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setStatus(UserStatus.valueOf(userDTO.getStatus()));

        return user;
    }

}
