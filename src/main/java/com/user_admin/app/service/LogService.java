package com.user_admin.app.service;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import com.user_admin.app.model.dto.mappers.RequestResponseLogMapper;
import com.user_admin.app.repository.RequestResponseLogRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
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

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    public List<RequestResponseLogDTO> getAllLogs() {
        List<RequestResponseLog> response = requestResponseLogRepository.findAll();
        return requestResponseLogMapper.toDtoList(response);
    }

    public List<RequestResponseLogDTO> findByCriteria(String endpoint, String method, Integer statusCode) {
        Optional<List<RequestResponseLog>> response = requestResponseLogRepository.findByCriteria(endpoint, method, statusCode);
        return response.map(requestResponseLogMapper::toDtoList).orElseGet(Collections::emptyList);
    }

    public List<RequestResponseLogDTO> getLogsByTimestamp(String start, String end) {
        LocalDateTime startDateTime = parseDate(start);
        LocalDateTime endDateTime = parseDate(end);

        List<RequestResponseLog> logs = requestResponseLogRepository.findByTimestampBetween(startDateTime, endDateTime);

        return requestResponseLogMapper.toDtoList(logs);
    }

    private LocalDateTime parseDate(String dateString) {
        for (int i = 0; i < FORMATTERS.size(); i++) {
            DateTimeFormatter formatter = FORMATTERS.get(i);
            try {
                if (i == FORMATTERS.size() - 1) {
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    return date.atStartOfDay();
                } else {
                    return LocalDateTime.parse(dateString, formatter);
                }
            } catch (DateTimeParseException e) {
                //ignoring error to try another format
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + dateString);
    }
}
