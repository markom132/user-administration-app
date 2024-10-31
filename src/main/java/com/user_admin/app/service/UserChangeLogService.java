package com.user_admin.app.service;

import com.user_admin.app.config.jwt.JwtUtil;
import com.user_admin.app.exceptions.ResourceNotFoundException;
import com.user_admin.app.model.User;
import com.user_admin.app.model.UserChangeLog;
import com.user_admin.app.model.dto.UserChangeLogDTO;
import com.user_admin.app.model.dto.mappers.UserChangeLogMapper;
import com.user_admin.app.repository.UserChangeLogRepository;
import com.user_admin.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user change logs.
 */
@Service
public class UserChangeLogService {

    private final UserChangeLogRepository userChangeLogRepository;
    private final UserChangeLogMapper userChangeLogMapper;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserChangeLogService.class);

    public UserChangeLogService(UserChangeLogRepository userChangeLogRepository, UserChangeLogMapper userChangeLogMapper, JwtUtil jwtUtil, UserRepository userRepository) {
        this.userChangeLogRepository = userChangeLogRepository;
        this.userChangeLogMapper = userChangeLogMapper;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the change logs for a specific user by user ID.
     *
     * @param id the ID of the user whose change logs are being retrieved
     * @return a list of UserChangeLogDTOs representing the user's change history
     */
    public List<UserChangeLogDTO> getUserChanges(Long id) {
        Optional<List<UserChangeLog>> userChanges = userChangeLogRepository.findByUserId(id);
        logger.info("Retrieved change logs for user ID: {}", id);
        return userChanges.map(userChangeLogMapper::toDtoList).orElseGet(Collections::emptyList);
    }

    /**
     * Logs a change made to a user's information.
     *
     * @param changeLog the UserChangeLog object containing the change information
     * @param user      the user whose information was changed
     * @param request   the HTTP request containing the authorization token
     * @throws ResourceNotFoundException if the user who made the change cannot be found
     */
    public void logChange(UserChangeLog changeLog, User user, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = authorizationHeader.substring(7);

        String changedByUserEmail = jwtUtil.extractUsername(token);
        User changedByUser = userRepository.findByEmail(changedByUserEmail)
                .orElseThrow(() -> {
                    logger.error("User with email: {} not found", changedByUserEmail);
                    return new ResourceNotFoundException("User with email: " + changedByUserEmail + " not found");
                });

        changeLog.setUser(user);
        changeLog.setChangedByFirstName(changedByUser.getFirstName());
        changeLog.setChangedByLastName(changedByUser.getLastName());

        userChangeLogRepository.save(changeLog);
        logger.info("Logged change for user ID: {} by user: {}", user.getId(), changedByUserEmail);
    }

    /**
     * Creates a UserChangeLog object with specified change details.
     *
     * @param fieldName the name of the field that was changed
     * @param oldValue  the previous value of the field
     * @param newValue  the new value of the field
     * @return a UserChangeLog object containing the change details
     */
    public UserChangeLog fillUserChangeLogDTO(String fieldName, String oldValue, String newValue) {
        UserChangeLog changeLog = new UserChangeLog();
        changeLog.setFieldName(fieldName);
        changeLog.setOldValue(oldValue);
        changeLog.setNewValue(newValue);
        changeLog.setChangedAt(LocalDateTime.now());

        logger.debug("Prepared UserChangeLog for field: {} with old value: {}, new value: {}", fieldName, oldValue, newValue);
        return changeLog;
    }

}
