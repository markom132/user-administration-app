package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseCaptureFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        CachedBodyHttpServletResponse responseWrapper = new CachedBodyHttpServletResponse((HttpServletResponse) response);
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        httpRequest.setAttribute("responseWrapper", responseWrapper);

        chain.doFilter(httpRequest, responseWrapper);

    }
}


