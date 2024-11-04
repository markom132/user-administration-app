package com.user_admin.app.services;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import com.user_admin.app.model.dto.mappers.RequestResponseLogMapper;
import com.user_admin.app.repository.RequestResponseLogRepository;
import com.user_admin.app.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogServiceTest {

    @Mock
    private RequestResponseLogRepository requestResponseLogRepository;

    @Mock
    private RequestResponseLogMapper requestResponseLogMapper;

    private LogService logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logService = new LogService(requestResponseLogRepository, requestResponseLogMapper);
    }

    @Test
    void testGetAllLogs_EmptyList() {
        List<RequestResponseLog> emptyLogList = List.of();

        when(requestResponseLogRepository.findAll()).thenReturn(emptyLogList);
        when(requestResponseLogMapper.toDtoList(emptyLogList)).thenReturn(List.of());

        List<RequestResponseLogDTO> retrievedLogs = logService.getAllLogs();

        assertNotNull(retrievedLogs);
        assertTrue(retrievedLogs.isEmpty());

        verify(requestResponseLogRepository).findAll();
        verify(requestResponseLogMapper).toDtoList(emptyLogList);
    }

    @Test
    void testGetAllLogs_NonEmptyList() {
        RequestResponseLog log1 = new RequestResponseLog();
        log1.setId(1L);
        log1.setRequestBody("Test request 1");
        log1.setResponseBody("Test response 1");

        RequestResponseLog log2 = new RequestResponseLog();
        log2.setId(2L);
        log2.setRequestBody("Test request 2");
        log2.setResponseBody("Test response 2");

        List<RequestResponseLog> logList = List.of(log1, log2);

        when(requestResponseLogRepository.findAll()).thenReturn(logList);

        RequestResponseLogDTO logDto1 = new RequestResponseLogDTO();
        logDto1.setRequestBody("Test request 1");
        logDto1.setResponseBody("Test response 1");

        RequestResponseLogDTO logDto2 = new RequestResponseLogDTO();
        logDto2.setRequestBody("Test request 2");
        logDto2.setResponseBody("Test response 2");

        List<RequestResponseLogDTO> dtoList = List.of(logDto1, logDto2);

        when(requestResponseLogMapper.toDtoList(logList)).thenReturn(dtoList);

        List<RequestResponseLogDTO> retrievedLogs = logService.getAllLogs();

        assertNotNull(retrievedLogs);
        assertEquals(2, retrievedLogs.size());
        assertEquals(logDto1.getRequestBody(), retrievedLogs.get(0).getRequestBody());
        assertEquals(logDto2.getRequestBody(), retrievedLogs.get(1).getRequestBody());

        verify(requestResponseLogRepository).findAll();
        verify(requestResponseLogMapper).toDtoList(logList);
    }

    @Test
    void findByCriteria_LogsFound_ReturnsLogDTOs() {
        String endpoint = "/api/test";
        String method = "GET";
        Integer statusCode = 200;

        RequestResponseLog log1 = new RequestResponseLog();
        log1.setRequestBody("Test request 1");
        log1.setResponseBody("Test response 1");

        RequestResponseLog log2 = new RequestResponseLog();
        log2.setRequestBody("Test request 2");
        log2.setResponseBody("Test response 2");

        List<RequestResponseLog> logList = List.of(log1, log2);
        Optional<List<RequestResponseLog>> optionalLogs = Optional.of(logList);

        when(requestResponseLogRepository.findByCriteria(endpoint, method, statusCode)).thenReturn(optionalLogs);
        when(requestResponseLogMapper.toDtoList(logList)).thenReturn(createTestLogDTOs());

        List<RequestResponseLogDTO> result = logService.findByCriteria(endpoint, method, statusCode);

        assertEquals(2, result.size());
        assertEquals("Test request 1", result.get(0).getRequestBody());
        assertEquals("Test response 1", result.get(0).getResponseBody());
        assertEquals("Test request 2", result.get(1).getRequestBody());
        assertEquals("Test response 2", result.get(1).getResponseBody());
    }

    @Test
    void testFindByCriteria_NoLogsFound() {
        String endpoint = "/api/test";
        String method = "GET";
        Integer statusCode = 404;

        when(requestResponseLogRepository.findByCriteria(endpoint, method, statusCode)).thenReturn(Optional.empty());

        List<RequestResponseLogDTO> retrievedLogs = logService.findByCriteria(endpoint, method, statusCode);

        assertNotNull(retrievedLogs);
        assertTrue(retrievedLogs.isEmpty());

        verify(requestResponseLogRepository).findByCriteria(endpoint, method, statusCode);
        verify(requestResponseLogMapper, never()).toDtoList(anyList());
    }

    private List<RequestResponseLogDTO> createTestLogDTOs() {
        RequestResponseLogDTO dto1 = new RequestResponseLogDTO();
        dto1.setRequestBody("Test request 1");
        dto1.setResponseBody("Test response 1");

        RequestResponseLogDTO dto2 = new RequestResponseLogDTO();
        dto2.setRequestBody("Test request 2");
        dto2.setResponseBody("Test response 2");

        return List.of(dto1, dto2);
    }

    @Test
    void getLogsByTimestamp_LogsFound_ReturnsLogDTOs() {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-31 23:59:59";

        LocalDateTime startDateTime = LocalDateTime.parse("2024-01-01T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse("2024-01-31T23:59:59");

        RequestResponseLog log1 = new RequestResponseLog();
        log1.setRequestBody("Test request 1");
        log1.setResponseBody("Test response 1");

        RequestResponseLog log2 = new RequestResponseLog();
        log2.setRequestBody("Test request 2");
        log2.setResponseBody("Test response 2");

        List<RequestResponseLog> logList = List.of(log1, log2);

        when(requestResponseLogRepository.findByTimestampBetween(startDateTime, endDateTime)).thenReturn(logList);
        when(requestResponseLogMapper.toDtoList(logList)).thenReturn(createTestLogDTOs());

        List<RequestResponseLogDTO> result = logService.getLogsByTimestamp(start, end);

        assertEquals(2, result.size());
        assertEquals("Test request 1", result.get(0).getRequestBody());
        assertEquals("Test response 1", result.get(0).getResponseBody());
        assertEquals("Test request 2", result.get(1).getRequestBody());
        assertEquals("Test response 2", result.get(1).getResponseBody());
    }

    @Test
    void getLogsByTimestamp_NoLogsFound_ReturnsEmptyList() {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-31 23:59:59";

        LocalDateTime startDateTime = LocalDateTime.parse("2024-01-01T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse("2024-01-31T23:59:59");

        when(requestResponseLogRepository.findByTimestampBetween(startDateTime, endDateTime)).thenReturn(List.of());

        List<RequestResponseLogDTO> result = logService.getLogsByTimestamp(start, end);

        assertEquals(0, result.size());
    }

    @Test
    void parseDate_ValidDateTime_ReturnsLocalDateTime() {
        String validDateTime1 = "2024-01-01 15:30:45"; // format: "yyyy-MM-dd HH:mm:ss"
        LocalDateTime result1 = logService.parseDate(validDateTime1);
        assertEquals(LocalDateTime.of(2024, 1, 1, 15, 30, 45), result1);

        String validDateTime2 = "2024-01-01 15:30"; // format: "yyyy-MM-dd HH:mm"
        LocalDateTime result2 = logService.parseDate(validDateTime2);
        assertEquals(LocalDateTime.of(2024, 1, 1, 15, 30), result2);

        String validDateTime3 = "2024-01-01"; // format: "yyyy-MM-dd"
        LocalDateTime result3 = logService.parseDate(validDateTime3);
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), result3);
    }

    @Test
    void parseDate_InvalidDate_ThrowsIllegalArgumentException() {
        String invalidDate1 = "Invalid Date String";
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            logService.parseDate(invalidDate1);
        });
        assertEquals("Invalid date format: " + invalidDate1, exception1.getMessage());

        String invalidDate2 = "2024-99-99"; // Invalid date
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            logService.parseDate(invalidDate2);
        });
        assertEquals("Invalid date format: " + invalidDate2, exception2.getMessage());
    }

    @Test
    void parseDate_ValidButDifferentFormat_ReturnsLocalDateTime() {
        String validDate1 = "2024-01-01"; // format: "yyyy-MM-dd"
        LocalDateTime result1 = logService.parseDate(validDate1);
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), result1);

        String validDate2 = "2024-01-01 12:00:00"; // format: "yyyy-MM-dd HH:mm:ss"
        LocalDateTime result2 = logService.parseDate(validDate2);
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), result2);
    }

}
