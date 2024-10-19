package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.PasswordResetToken;
import com.user_admin.app.model.User;
import com.user_admin.app.model.dto.PasswordResetTokenDTO;
import com.user_admin.app.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenMapper {

    private final UserRepository userRepository;

    public PasswordResetTokenMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PasswordResetTokenDTO toDTO(PasswordResetToken passwordResetToken) {
        if (passwordResetToken == null) {
            return null;
        }

        return new PasswordResetTokenDTO(
                passwordResetToken.getId(),
                passwordResetToken.getToken(),
                passwordResetToken.getCreatedAt(),
                passwordResetToken.getExpiresAt(),
                passwordResetToken.getUser().getId()
        );
    }

    public PasswordResetToken toEntity(PasswordResetTokenDTO passwordResetTokenDTO) {
        if (passwordResetTokenDTO == null) {
            return null;
        }

        User user = userRepository.findById(passwordResetTokenDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setId(passwordResetTokenDTO.getId());
        passwordResetToken.setToken(passwordResetTokenDTO.getToken());
        passwordResetToken.setCreatedAt(passwordResetTokenDTO.getCreatedAt());
        passwordResetToken.setExpiresAt(passwordResetTokenDTO.getExpiresAt());
        passwordResetToken.setUser(user);

        return passwordResetToken;
    }

}
