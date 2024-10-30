package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.model.dto.AuthTokenDTO;
import com.user_admin.app.model.dto.PasswordResetTokenDTO;
import com.user_admin.app.model.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User entities and UserDTOs.
 */
@Component
public class UserMapper {

    private static final Logger logger = LoggerFactory.getLogger(UserMapper.class);

    private final AuthTokenMapper authTokenMapper;
    private final PasswordResetTokenMapper passwordResetTokenMapper;

    public UserMapper(AuthTokenMapper authTokenMapper, PasswordResetTokenMapper passwordResetTokenMapper) {
        this.authTokenMapper = authTokenMapper;
        this.passwordResetTokenMapper = passwordResetTokenMapper;
    }

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the User entity to convert
     * @return UserDTO with user details, or null if the user is null
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            logger.warn("Attempted to convert a null User to UserDTO");
            return null;
        }

        List<AuthTokenDTO> authTokens = user.getAuthTokens() != null ?
                user.getAuthTokens().stream().map(authTokenMapper::toDTO)
                        .collect(Collectors.toList()) : null;

        List<PasswordResetTokenDTO> passwordResetTokens = user.getPasswordResetTokens() != null ?
                user.getPasswordResetTokens().stream().map(passwordResetTokenMapper::toDTO)
                        .collect(Collectors.toList()) : null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCreatedAt = user.getCreatedAt().format(formatter);
        String formattedUpdatedAt = user.getUpdatedAt().format(formatter);

        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus().name(),
                authTokens,
                passwordResetTokens,
                formattedCreatedAt,
                formattedUpdatedAt
        );
    }

    /**
     * Convert a list of User entities to a list of UserDTOs.
     *
     * @param users the list of User entities to convert
     * @return a list of UserDTOs
     */
    public List<UserDTO> toDtoList(List<User> users) {
        logger.info("Converting list of User entities to list of UserDTOs. Total users: {}", users.size());
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a UserDTO to a User entity.
     *
     * @param userDTO the UserDTO to convert
     * @return User entity, or null if userDTO is null
     */
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            logger.warn("Attempted to convert a null UserDTO to User");
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirstName().toUpperCase());
        user.setLastName(userDTO.getLastName().toUpperCase());
        user.setEmail(userDTO.getEmail());
        user.setStatus(UserStatus.valueOf(userDTO.getStatus()));

        return user;
    }

}
