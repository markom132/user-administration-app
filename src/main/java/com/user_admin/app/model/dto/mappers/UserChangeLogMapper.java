package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserChangeLog;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between UserChangeLog entities and UserChangeLogDTOs.
 */
@Component
public class UserChangeLogMapper {

    private static final Logger logger = LoggerFactory.getLogger(UserChangeLogMapper.class);

    private final UserRepository userRepository;

    public UserChangeLogMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Converts a UserChangeLog entity to a UserChangeLogDTO.
     *
     * @param userChangeLog the UserChangeLog entity to convert
     * @return UserChangeLogDTO with changeLog details, or null if userChangeLog is null
     */
    public UserChangeLogDTO toDTO(UserChangeLog userChangeLog) {
        if (userChangeLog == null) {
            logger.warn("Attempted to convert a null UserChangeLog to UserChangeLogDTO");
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedChangedAt = userChangeLog.getChangedAt().format(formatter);

        return new UserChangeLogDTO(
                userChangeLog.getUser() != null ? userChangeLog.getUser().getId() : null,
                userChangeLog.getFieldName(),
                userChangeLog.getOldValue(),
                userChangeLog.getNewValue(),
                formattedChangedAt,
                userChangeLog.getChangedByFirstName(),
                userChangeLog.getChangedByFirstName()
        );
    }

    /**
     * Converts a list of UserChangeLog entities to a list of UserChangeLogDTOs.
     *
     * @param userChangeLogs the list of UserChangeLog entities to convert
     * @return a list of UserChangeLogDTOs
     */
    public List<UserChangeLogDTO> toDtoList(List<UserChangeLog> userChangeLogs) {
        logger.info("Converting list of UserChangeLog entities to list of UserChangeLogDTOs. Total logs: {}", userChangeLogs.size());
        return userChangeLogs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a UserChangeLogDTO to a UserChangeLog entity.
     *
     * @param userChangeLogDTO the UserChangeLogDTO to convert
     * @return UserChangeLog entity, or null if userChangeLogDTO is null
     */
    public UserChangeLog toEntity(UserChangeLogDTO userChangeLogDTO) {
        if (userChangeLogDTO == null) {
            logger.warn("Attempted to convert a null UserChangeLogDTO to UserChangeLog");
            return null;
        }

        User user = userRepository.findById(userChangeLogDTO.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found for ID: {}", userChangeLogDTO.getUserId());
                    return new RuntimeException("User not found");
                });

        UserChangeLog userChangeLog = new UserChangeLog();
        userChangeLog.setUser(user);
        userChangeLog.setFieldName(userChangeLogDTO.getFieldName());
        userChangeLog.setOldValue(userChangeLogDTO.getOldValue());
        userChangeLog.setNewValue(userChangeLogDTO.getNewValue());
        userChangeLog.setChangedAt(LocalDateTime.parse(userChangeLogDTO.getChangedAt()));
        userChangeLog.setChangedByFirstName(userChangeLog.getChangedByFirstName());
        userChangeLog.setChangedByLastName(userChangeLog.getChangedByLastName());

        return userChangeLog;
    }

}
