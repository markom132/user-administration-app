package com.user_admin.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {

    private String createdAt;
    private String userEmail;
    private String api;
    private String request;
    private String message;
    private String codeLine;
    private Map<String, Object> entryParams;

}
