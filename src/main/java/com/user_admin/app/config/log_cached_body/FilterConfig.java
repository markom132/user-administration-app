package com.user_admin.app.config.log_cached_body;

import com.user_admin.app.config.log_cached_body.request.RequestBodyCacheFilter;
import com.user_admin.app.config.log_cached_body.response.ResponseCaptureFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestBodyCacheFilter> requestBodyCacheFilter() {
        FilterRegistrationBean<RequestBodyCacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestBodyCacheFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ResponseCaptureFilter> responseBodyCacheFilter() {
        FilterRegistrationBean<ResponseCaptureFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ResponseCaptureFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        return registrationBean;
    }
}
