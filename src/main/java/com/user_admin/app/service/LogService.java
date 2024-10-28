package com.user_admin.app.service;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.repository.RequestResponseLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final RequestResponseLogRepository requestResponseLogRepository;

    public LogService(RequestResponseLogRepository requestResponseLogRepository) {
        this.requestResponseLogRepository = requestResponseLogRepository;
    }

    public List<RequestResponseLog> getAllLogs() {
        return requestResponseLogRepository.findAll();
    }

    public List<RequestResponseLog> findByCriteria(String endpoint, String method, int statusCode) {
        return requestResponseLogRepository.findByCriteria(endpoint, method, statusCode);
    }
}
