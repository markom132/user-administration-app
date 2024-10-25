package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChangeLogDTO {

    private Long userId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedAt;
    private String changedByFirstName;
    private String changedByLastName;

}
