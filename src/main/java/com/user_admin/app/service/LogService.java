package com.user_admin.app.service;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import com.user_admin.app.model.dto.mappers.RequestResponseLogMapper;
import com.user_admin.app.repository.RequestResponseLogRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class LogService {

    private final RequestResponseLogRepository requestResponseLogRepository;
    private final RequestResponseLogMapper requestResponseLogMapper;

    public LogService(RequestResponseLogRepository requestResponseLogRepository, RequestResponseLogMapper requestResponseLogMapper) {
        this.requestResponseLogRepository = requestResponseLogRepository;
        this.requestResponseLogMapper = requestResponseLogMapper;
    }

    public List<RequestResponseLogDTO> getAllLogs() {
        List<RequestResponseLog> response = requestResponseLogRepository.findAll();
        return requestResponseLogMapper.toDtoList(response);
    }

    public List<RequestResponseLogDTO> findByCriteria(String endpoint, String method, Integer statusCode) {
        Optional<List<RequestResponseLog>> response = requestResponseLogRepository.findByCriteria(endpoint, method, statusCode);
        return response.map(requestResponseLogMapper::toDtoList).orElseGet(Collections::emptyList);
    }
}
