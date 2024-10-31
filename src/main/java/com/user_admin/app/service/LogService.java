package com.user_admin.app.service;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import com.user_admin.app.model.dto.mappers.RequestResponseLogMapper;
import com.user_admin.app.repository.RequestResponseLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing request and response logs.
 */
@Service
public class LogService {

    private final RequestResponseLogRepository requestResponseLogRepository;
    private final RequestResponseLogMapper requestResponseLogMapper;
    private final Logger logger = LoggerFactory.getLogger(LogService.class);

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

    /**
     * Retrieves all request/response logs.
     *
     * @returna list of RequestResponseLogDTO objects containing all logs
     */
    public List<RequestResponseLogDTO> getAllLogs() {
        List<RequestResponseLog> response = requestResponseLogRepository.findAll();
        logger.info("Retrieved all logs, count: {}", response.size());
        return requestResponseLogMapper.toDtoList(response);
    }

    /**
     * Finds logs based on specified criteria.
     *
     * @param endpoint   the endpoint to filter logs by
     * @param method     the HTTP method to filter logs by
     * @param statusCode the HTTP status code to filter logs by
     * @return a list of RequestResponseLogDTO matching the criteria, or an empty list if none found
     */
    public List<RequestResponseLogDTO> findByCriteria(String endpoint, String method, Integer statusCode) {
        Optional<List<RequestResponseLog>> response = requestResponseLogRepository.findByCriteria(endpoint, method, statusCode);
        if (response.isPresent()) {
            logger.info("Found {} logs for criteria: endpoint={}, method={}, statusCode={}", response.get().size(), endpoint, method, statusCode);
        } else {
            logger.info("No logs found for criteria: endpoint={}, method={}, statusCode={}", endpoint, method, statusCode);
        }
        return response.map(requestResponseLogMapper::toDtoList).orElseGet(Collections::emptyList);
    }

    /**
     * Retrieves logs that fall within the specified timestamp range.
     *
     * @param start teh start of the timestamp range (inclusive)
     * @param end   the end of the timestamp range (inclusive)
     * @return a list of RequestResponseLogDTO containing logs within the specified range
     */
    public List<RequestResponseLogDTO> getLogsByTimestamp(String start, String end) {
        LocalDateTime startDateTime = parseDate(start);
        LocalDateTime endDateTime = parseDate(end);

        logger.info("Retrieving logs between {} and {}", startDateTime, endDateTime);
        List<RequestResponseLog> logs = requestResponseLogRepository.findByTimestampBetween(startDateTime, endDateTime);
        logger.info("Retrieved {} logs between {} and {}", logs.size(), startDateTime, endDateTime);

        return requestResponseLogMapper.toDtoList(logs);
    }

    /**
     * Parses a date string into a LocalDateTime object using predefined formatters.
     *
     * @param dateString teh date string to parse
     * @return the parsed LocalDateTime object
     * @throws IllegalArgumentException if the date string cannot be parsed
     */
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
                logger.warn("Failed to parse date '{}', trying next format", dateString);
            }
        }
        logger.error("Invalid date format: {}", dateString);
        throw new IllegalArgumentException("Invalid date format: " + dateString);
    }
}
