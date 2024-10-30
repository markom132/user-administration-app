package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.dto.PasswordResetTokenDTO;
import com.user_admin.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between PasswordResetToken entities and PasswordResetTokenDTOs.
 */
@Component
public class PasswordResetTokenMapper {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenMapper.class);

    private final UserRepository userRepository;

    public PasswordResetTokenMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Converts a PasswordResetToken entity to a PasswordResetTokenDTO.
     *
     * @param passwordResetToken the PasswordResetToken entity to convert
     * @return PasswordResetTokenDTO with token details, or null if passwordResetToken is null
     */
    public PasswordResetTokenDTO toDTO(PasswordResetToken passwordResetToken) {
        if (passwordResetToken == null) {
            logger.warn("Attempted to convert a null PasswordResetToken to PasswordResetTokenDTO");
            return null;
        }

        logger.info("Converting PasswordResetToken entity with ID {} to PasswordResetTokenDTO", passwordResetToken.getId());
        return new PasswordResetTokenDTO(
                passwordResetToken.getId(),
                passwordResetToken.getToken(),
                passwordResetToken.getCreatedAt(),
                passwordResetToken.getExpiresAt(),
                passwordResetToken.getUser().getId()
        );
    }

    /**
     * Converts a PasswordResetTokenDTO to a PasswordResetToken entity.
     *
     * @param passwordResetTokenDTO the PasswordResetTokenDTO to convert
     * @return PasswordResetToken entity, or null if passwordResetTokenDTO is null
     * @throws RuntimeException if the user associated with the token ID is not found
     */
    public PasswordResetToken toEntity(PasswordResetTokenDTO passwordResetTokenDTO) {
        if (passwordResetTokenDTO == null) {
            logger.warn("Attempted to convert a null PasswordResetTokenDTO to PasswordResetToken");
            return null;
        }

        logger.info("Converting PasswordResetTokenDTO with ID {} to PasswordResetToken entity", passwordResetTokenDTO.getId());

        User user = userRepository.findById(passwordResetTokenDTO.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found for ID: {}", passwordResetTokenDTO.getUserId());
                    return new RuntimeException("User not found");
                });

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setId(passwordResetTokenDTO.getId());
        passwordResetToken.setToken(passwordResetTokenDTO.getToken());
        passwordResetToken.setCreatedAt(passwordResetTokenDTO.getCreatedAt());
        passwordResetToken.setExpiresAt(passwordResetTokenDTO.getExpiresAt());
        passwordResetToken.setUser(user);

        logger.info("Successfully converted PasswordResetTokenDTO with ID {} to PasswordResetToken entity", passwordResetTokenDTO.getId());
        return passwordResetToken;
    }

}
