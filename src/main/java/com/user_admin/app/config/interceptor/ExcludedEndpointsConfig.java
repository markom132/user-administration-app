package com.user_admin.app.config.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Config class for managing endpoints that are excluded from logging.
 * <p>
 * The excluded endpoints are fetched from the application properties
 * under the property key `excluded.log.endpoints`.
 * </p>
 */
@Component
public class ExcludedEndpointsConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExcludedEndpointsConfig.class);

    @Value("${excluded.log.endpoints}")
    private String excludedEndpoint;

    /**
     * Retrieves a list of endpoints to be excluded from logging.
     * The list is derived by splitting the `excludedEndpoints` string on commas.
     *
     * @return List of excluded endpoint paths.
     */
    public List<String> getExcludedEndpoint() {
        List<String> excludedEndpoints = Arrays.asList(excludedEndpoint.split(","));
        logger.debug("Excluded endpoints list: {}", excludedEndpoints);

        return excludedEndpoints;
    }
}
