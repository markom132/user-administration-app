package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.dto.AuthTokenDTO;
import com.user_admin.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AuthToken entities and AuthTokenDTOs.
 */
@Component
public class AuthTokenMapper {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenMapper.class);

    private final UserRepository userRepository;

    public AuthTokenMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Converts an AuthToken entity to an AuthTokenDTO.
     *
     * @param authToken the AuthToken entity to be converted
     * @return AuthTokenDTO containing token details, or null if authToken is null
     */
    public AuthTokenDTO toDTO(AuthToken authToken) {
        if (authToken == null) {
            logger.warn("Attempted to convert a null AuthToken to AuthTokenDTO");
            return null;
        }

        logger.info("Converting AuthToken entity with ID {} to AuthTokenDTO", authToken.getId());
        return new AuthTokenDTO(
                authToken.getId(),
                authToken.getToken(),
                authToken.getCreatedAt(),
                authToken.getLastUsedAt(),
                authToken.getExpiresAt(),
                authToken.getUser().getId()
        );
    }

    /**
     * Converts an AuthTokenDTO to an AuthToken entity.
     *
     * @param authTokenDTO the AuthTokenDTO to be converted
     * @return AuthToken entity containing token details, or null if authTokenDTO is null
     * @throws RuntimeException if the user associated with the token ID is not found
     */
    public AuthToken toEntity(AuthTokenDTO authTokenDTO) {
        if (authTokenDTO == null) {
            logger.warn("Attempted to convert a null AuthTokenDTO to AuthToken");
            return null;
        }

        logger.info("Converting AuthTokenDTO with ID {} to AuthToken entity", authTokenDTO.getId());

        User user = userRepository.findById(authTokenDTO.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found for ID: {}", authTokenDTO.getUserId());
                    return new RuntimeException("User not found");
                });

        AuthToken authToken = new AuthToken();
        authToken.setId(authTokenDTO.getId());
        authToken.setToken(authTokenDTO.getToken());
        authToken.setCreatedAt(authTokenDTO.getCreatedAt());
        authToken.setLastUsedAt(authTokenDTO.getLastUsedAt());
        authToken.setExpiresAt(authTokenDTO.getExpiresAt());
        authToken.setUser(user);

        logger.info("Successfully converted AuthTokenDTO with ID {} to AuthToken entity", authTokenDTO.getId());
        return authToken;
    }

}
