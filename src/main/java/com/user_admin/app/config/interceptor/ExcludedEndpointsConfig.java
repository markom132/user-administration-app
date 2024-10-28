package com.user_admin.app.config.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ExcludedEndpointsConfig {

    @Value("${excluded.log.endpoints}")
    private String excludedEndpoint;

    public List<String> getExcludedEndpoint() {
        return Arrays.asList(excludedEndpoint.split(","));
    }
}
