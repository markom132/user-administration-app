package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserChangeLog;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.model.dto.UserDTO;
import com.user_admin.app.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
                userChangeLog.getUser() != null ? userChangeLog.getUser().getId() : null,
                userChangeLog.getFieldName(),
                userChangeLog.getOldValue(),
                userChangeLog.getNewValue(),
                userChangeLog.getChangedAt(),
                userChangeLog.getChangedByFirstName(),
                userChangeLog.getChangedByFirstName()
        );
    }

    public List<UserChangeLogDTO> toDtoList(List<UserChangeLog> userChangeLogs) {
        return userChangeLogs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserChangeLog toEntity(UserChangeLogDTO userChangeLogDTO) {
        if (userChangeLogDTO == null) {
            return null;
        }

        User user = userRepository.findById(userChangeLogDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserChangeLog userChangeLog = new UserChangeLog();
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
