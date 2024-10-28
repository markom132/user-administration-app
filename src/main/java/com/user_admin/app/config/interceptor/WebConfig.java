package com.user_admin.app.config.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class responsible for registering application-wide interceptors.
 * This class registers the LogInterceptor to capture request and response data for
 * all incoming HTTP requests.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;

    /**
     * Constructs the WebConfig instance with the specified LogInterceptor.
     *
     * @param logInterceptor the LogInterceptor to be registered in the application
     */
    public WebConfig(LogInterceptor logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    /**
     * Adds the LogInterceptor to intercept requests across all URL patterns.
     * This allows for logging and analysis of request and response data for all endpoints.
     *
     * @param registry the InterceptorRegistry to which the LogInterceptor is added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the LogInterceptor to intercept requests on all URL paths
        registry.addInterceptor(logInterceptor).addPathPatterns("/**");
    }
}
