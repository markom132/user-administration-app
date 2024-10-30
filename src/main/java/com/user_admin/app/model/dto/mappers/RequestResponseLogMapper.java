package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between RequestResponseLog entities and RequestResponseLogDTOs.
 */
@Component
public class RequestResponseLogMapper {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLogMapper.class);

    /**
     * Converts a RequestResponseLog entity to a RequestResponseLogDTO.
     *
     * @param requestResponseLog the RequestResponseLog entity to convert
     * @return RequestResponseLogDTO with log details, or null if requestResponseLog is null
     */
    public RequestResponseLogDTO toDTO(RequestResponseLog requestResponseLog) {
        if (requestResponseLog == null) {
            logger.warn("Attempted to convert a null RequestResponseLog to RequestResponseLogDTO");
            return null;
        }

        return new RequestResponseLogDTO(
                requestResponseLog.getMethod(),
                requestResponseLog.getEndpoint(),
                requestResponseLog.getRequestBody(),
                requestResponseLog.getResponseBody(),
                requestResponseLog.getStatusCode(),
                requestResponseLog.getTimestamp(),
                requestResponseLog.getExecutionTime()
        );
    }

    /**
     * Converts a list of RequestResponseLog entities to a list of RequestResponseLogDTOs.
     *
     * @param logs the list of RequestResponseLog entities to convert
     * @return a list of RequestResponseLogDTOs
     */
    public List<RequestResponseLogDTO> toDtoList(List<RequestResponseLog> logs) {
        logger.info("Converting a list of RequestResponseLog entities to list of RequestResponseLogDTOs. Total logs: {}", logs.size());
        return logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a RequestResponseLogDTO to a RequestResponseLog entity.
     *
     * @param requestResponseLogDTO the RequestResponseLogDTO to convert
     * @return RequestResponseLog entity, or null if RequestResponseLogDTO is null
     */
    public RequestResponseLog toEntity(RequestResponseLogDTO requestResponseLogDTO) {
        if (requestResponseLogDTO == null) {
            logger.warn("Attempted to convert a null RequestResponseLogDTO to RequestResponseLog ");
            return null;
        }

        logger.info("Converting RequestResponseLogDTO with method {} and endpoint {} to entity", requestResponseLogDTO.getMethod(), requestResponseLogDTO.getEndpoint());
        RequestResponseLog requestResponseLog = new RequestResponseLog();
        requestResponseLog.setMethod(requestResponseLogDTO.getMethod());
        requestResponseLog.setEndpoint(requestResponseLogDTO.getEndpoint());
        requestResponseLog.setRequestBody(requestResponseLogDTO.getRequestBody());
        requestResponseLog.setResponseBody(requestResponseLogDTO.getResponseBody());
        requestResponseLog.setStatusCode(requestResponseLogDTO.getStatusCode());
        requestResponseLog.setTimestamp(requestResponseLogDTO.getTimestamp());
        requestResponseLog.setExecutionTime(requestResponseLogDTO.getExecutionTime());

        return requestResponseLog;
    }
}
