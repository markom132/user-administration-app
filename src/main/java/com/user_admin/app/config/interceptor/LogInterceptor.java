package com.user_admin.app.config.interceptor;

import com.user_admin.app.config.log_cached_body.request.CachedBodyHttpServletRequest;
import com.user_admin.app.config.log_cached_body.response.CachedBodyHttpServletResponse;
import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.repository.RequestResponseLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private final RequestResponseLogRepository requestResponseLogRepository;

    public LogInterceptor(RequestResponseLogRepository requestResponseLogRepository) {
        this.requestResponseLogRepository = requestResponseLogRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);

        String requestBody = new String(cachedBodyHttpServletRequest.getInputStream().readAllBytes());


        RequestResponseLog log = new RequestResponseLog();
        log.setMethod(request.getMethod());
        log.setEndpoint(request.getRequestURI());
        log.setRequestBody(requestBody);
        log.setTimestamp(LocalDateTime.now());

        requestResponseLogRepository.save(log);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws IOException {
        CachedBodyHttpServletResponse responseWrapper = (CachedBodyHttpServletResponse) request.getAttribute("responseWrapper");

        if (responseWrapper != null) {
            String responseBody = new String(responseWrapper.getCachedContent());
            System.out.println("Captured Response Body: " + responseBody);

            RequestResponseLog log = requestResponseLogRepository.findTopByEndpointAndMethodOrderByTimestampDesc(request.getRequestURI(), request.getMethod());

            if (log != null) {
                log.setStatusCode(response.getStatus());
                log.setResponseTimestamp(LocalDateTime.now());
                log.setResponseBody(responseBody);

                try {
                    requestResponseLogRepository.save(log);
                } catch (Exception e) {
                    System.out.println("Error saving log: " + e.getMessage());
                }
            }
        }
    }

}
