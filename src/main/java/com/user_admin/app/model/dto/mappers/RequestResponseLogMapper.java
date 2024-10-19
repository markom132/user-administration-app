package com.user_admin.app.model.dto.mappers;

import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.model.dto.RequestResponseLogDTO;
import org.springframework.stereotype.Component;

@Component
public class RequestResponseLogMapper {

    public RequestResponseLogDTO toDTO(RequestResponseLog requestResponseLog) {
        if (requestResponseLog == null) {
            return null;
        }

        return new RequestResponseLogDTO(
                requestResponseLog.getId(),
                requestResponseLog.getMethod(),
                requestResponseLog.getUrl(),
                requestResponseLog.getRequestBody(),
                requestResponseLog.getResponseBody(),
                requestResponseLog.getStatusCode(),
                requestResponseLog.getTimestamp(),
                requestResponseLog.getExecutionTime()
        );
    }

    public RequestResponseLog toEntity(RequestResponseLogDTO requestResponseLogDTO) {
        if (requestResponseLogDTO == null) {
            return null;
        }

        RequestResponseLog requestResponseLog = new RequestResponseLog();
        requestResponseLog.setId(requestResponseLogDTO.getId());
        requestResponseLog.setMethod(requestResponseLogDTO.getMethod());
        requestResponseLog.setUrl(requestResponseLogDTO.getUrl());
        requestResponseLog.setRequestBody(requestResponseLogDTO.getRequestBody());
        requestResponseLog.setResponseBody(requestResponseLogDTO.getResponseBody());
        requestResponseLog.setStatusCode(requestResponseLogDTO.getStatusCode());
        requestResponseLog.setTimestamp(requestResponseLogDTO.getTimestamp());
        requestResponseLog.setExecutionTime(requestResponseLogDTO.getExecutionTime());

        return requestResponseLog;
    }

}
