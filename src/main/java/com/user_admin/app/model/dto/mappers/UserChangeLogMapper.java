package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserChangeLog;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserChangeLogMapper {

    private final UserRepository userRepository;

    public UserChangeLogMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserChangeLogDTO toDTO(UserChangeLog userChangeLog) {
        if (userChangeLog == null) {
            return null;
        }
        return new UserChangeLogDTO(
                userChangeLog.getId(),
                userChangeLog.getUser() != null ? userChangeLog.getUser().getId() : null,
                userChangeLog.getFieldName(),
                userChangeLog.getOldValue(),
                userChangeLog.getNewValue(),
                userChangeLog.getChangedAt(),
                userChangeLog.getChangedByFirstName(),
                userChangeLog.getChangedByFirstName()
        );
    }

    public UserChangeLog toEntity(UserChangeLogDTO userChangeLogDTO) {
        if (userChangeLogDTO == null) {
            return null;
        }

        User user = userRepository.findById(userChangeLogDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserChangeLog userChangeLog = new UserChangeLog();
        userChangeLog.setId(userChangeLogDTO.getId());
        userChangeLog.setUser(user);
        userChangeLog.setFieldName(userChangeLogDTO.getFieldName());
        userChangeLog.setOldValue(userChangeLogDTO.getOldValue());
        userChangeLog.setNewValue(userChangeLogDTO.getNewValue());
        userChangeLog.setChangedAt(userChangeLogDTO.getChangedAt());
        userChangeLog.setChangedByFirstName(userChangeLog.getChangedByFirstName());
        userChangeLog.setChangedByLastName(userChangeLog.getChangedByLastName());

        return userChangeLog;
    }

}
