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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserChangeLogService {

    private final UserChangeLogRepository userChangeLogRepository;
    private final UserChangeLogMapper userChangeLogMapper;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public UserChangeLogService(UserChangeLogRepository userChangeLogRepository, UserChangeLogMapper userChangeLogMapper, JwtUtil jwtUtil, UserRepository userRepository) {
        this.userChangeLogRepository = userChangeLogRepository;
        this.userChangeLogMapper = userChangeLogMapper;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public List<UserChangeLogDTO> getUserChanges(Long id) {
        Optional<List<UserChangeLog>> userChanges = userChangeLogRepository.findByUserId(id);

        return userChanges.map(userChangeLogMapper::toDtoList).orElseGet(Collections::emptyList);
    }

    public void logChange(UserChangeLog changeLog, User user, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = authorizationHeader.substring(7);

        String changedByUserEmail = jwtUtil.extractUsername(token);
        User changedByUser = userRepository.findByEmail(changedByUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + changedByUserEmail + " not found"));

        changeLog.setUser(user);
        changeLog.setChangedByFirstName(changedByUser.getFirstName());
        changeLog.setChangedByLastName(changedByUser.getLastName());

        userChangeLogRepository.save(changeLog);
    }

    public UserChangeLog fillUserChangeLogDTO(String fieldName, String oldValue, String newValue){
        UserChangeLog changeLog = new UserChangeLog();
        changeLog.setFieldName(fieldName);
        changeLog.setOldValue(oldValue);
        changeLog.setNewValue(newValue);
        changeLog.setChangedAt(LocalDateTime.now());

        return changeLog;
    }

}
