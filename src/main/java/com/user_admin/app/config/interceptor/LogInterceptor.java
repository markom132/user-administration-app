package com.user_admin.app.config.interceptor;

import com.user_admin.app.config.log_cached_body.request.CachedBodyHttpServletRequest;
import com.user_admin.app.config.log_cached_body.response.CachedBodyHttpServletResponse;
import com.user_admin.app.model.RequestResponseLog;
import com.user_admin.app.repository.RequestResponseLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * Interceptor for logging request and response details.
 * <p>
 * This interceptor logs requests and responses for auditing and monitoring purposes,
 * excluding certain endpoints configured in {@link ExcludedEndpointsConfig}.
 * </p>
 */
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    private final RequestResponseLogRepository requestResponseLogRepository;

    @Autowired
    private ExcludedEndpointsConfig excludedEndpointsConfig;

    /**
     * Constructs the LogInterceptor with the required repository.
     *
     * @param requestResponseLogRepository Repository for storing request-response logs.
     */
    public LogInterceptor(RequestResponseLogRepository requestResponseLogRepository) {
        this.requestResponseLogRepository = requestResponseLogRepository;
    }

    /**
     * Intercepts each incoming request to log its details before processing.
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param handler  the chosen handler to execute, for type and/or instance evaluation
     * @return true if the request should proceed to the handler, false otherwise
     * @throws IOException if an input or output error occurs while reading the request body
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String requestUri = request.getRequestURI();
        logger.info("Received request for URI: {}", requestUri);

        // Check if the endpoint is excluded from logging
        if (excludedEndpointsConfig.getExcludedEndpoint().contains(requestUri)) {
            logger.info("Endpoint {} is excluded from logging.", requestUri);
            return true;
        }

        // Wrap request to cache the body for logging
        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);

        String requestBody = new String(cachedBodyHttpServletRequest.getInputStream().readAllBytes());

        // Log request details
        RequestResponseLog log = new RequestResponseLog();
        log.setMethod(request.getMethod());
        log.setEndpoint(request.getRequestURI());
        log.setRequestBody(requestBody);
        log.setTimestamp(LocalDateTime.now());

        // Save the log entry to the database
        requestResponseLogRepository.save(log);
        logger.info("Request details saved to log: [Method: {}, URI: {}]", request.getMethod(), requestUri);

        return true;
    }

    /**
     * Finalizes request logging after request completion, capturing response details.
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param handler  the chosen handler to execute
     * @param ex       any exception thrown on handler execution, if any; this does not
     *                 include exceptions that have been handled through an exception resolver
     * @throws IOException if an input or output error occurs while reading the response body
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws IOException {
        String requestUri = request.getRequestURI();

        // Check if the endpoint is excluded from logging
        if (excludedEndpointsConfig.getExcludedEndpoint().contains(requestUri)) {
            return;
        }
        // Retrieve the wrapped response to access cached response body content
        CachedBodyHttpServletResponse responseWrapper = (CachedBodyHttpServletResponse) request.getAttribute("responseWrapper");

        if (responseWrapper != null) {
            String responseBody = new String(responseWrapper.getCachedContent());
            logger.info("Captured response body for logging");

            // Find the latest log entry for this specific request URI and method
            RequestResponseLog log = requestResponseLogRepository.findTopByEndpointAndMethodOrderByTimestampDesc(request.getRequestURI(), request.getMethod());

            if (log != null) {
                LocalDateTime requestTimestamp = log.getTimestamp();
                LocalDateTime responseTimestamp = LocalDateTime.now();
                Long executionTime = Duration.between(requestTimestamp, responseTimestamp).toMillis();

                // Populate log details with response data
                log.setStatusCode(response.getStatus());
                log.setResponseTimestamp(responseTimestamp);
                log.setExecutionTime(executionTime);
                log.setResponseBody(responseBody);

                try {
                    // Save the updated log entry
                    requestResponseLogRepository.save(log);
                    logger.info("Log saved successfully for URI: {}, with status: {}", request.getRequestURI(), response.getStatus());
                } catch (Exception e) {
                    logger.error("Error saving log for URI: {} - {}", request.getRequestURI(), e.getMessage());
                }
            } else {
                logger.warn("No matching log entry found for URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
            }
        } else {
            logger.warn("No response wrapper available, unable to capture response body.");
        }
    }

}
