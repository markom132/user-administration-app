package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.AuthToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.dto.AuthTokenDTO;
import com.user_admin.app.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenMapper {

    private final UserRepository userRepository;

    public AuthTokenMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthTokenDTO toDTO(AuthToken authToken) {
        if (authToken == null) {
            return null;
        }

        return new AuthTokenDTO(
                authToken.getId(),
                authToken.getToken(),
                authToken.getCreatedAt(),
                authToken.getLastUsedAt(),
                authToken.getExpiresAt(),
                authToken.getUser().getId()
        );
    }

    public AuthToken toEntity(AuthTokenDTO authTokenDTO) {
        if (authTokenDTO == null) {
            return null;
        }

        User user = userRepository.findById(authTokenDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthToken authToken = new AuthToken();
        authToken.setId(authTokenDTO.getId());
        authToken.setToken(authTokenDTO.getToken());
        authToken.setCreatedAt(authTokenDTO.getCreatedAt());
        authToken.setLastUsedAt(authTokenDTO.getLastUsedAt());
        authToken.setExpiresAt(authTokenDTO.getExpiresAt());
        authToken.setUser(user);

        return authToken;
    }

}
