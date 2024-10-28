package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter that wraps the HttpServletResponse to allow capturing and caching the response body context.
 * This cached response is useful for logging or further processing after request completion.
 */
public class ResponseCaptureFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCaptureFilter.class);

    /**
     * Wraps the HttpServletResponse in a CachedBodyHttpServletResponse to capture response content.
     *
     * @param request  the incoming HttpServletRequest
     * @param response the outgoing HttpServletResponse, wrapped to capture body content
     * @param chain    the filter chain for processing the request
     * @throws IOException      if an I/O error occurs during processing
     * @throws ServletException if a servlet error occurs during processing
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        logger.debug("Initializing response capture filter for request: {}", ((HttpServletRequest) request).getRequestURI());

        // Wraps the response to capture its content
        CachedBodyHttpServletResponse responseWrapper = new CachedBodyHttpServletResponse((HttpServletResponse) response);

        // Cast the request and set the wrapped response in the request attributes for later access
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        httpRequest.setAttribute("responseWrapper", responseWrapper);
        logger.debug("CachedBodyHttpServletResponse set in request attributes.");

        chain.doFilter(httpRequest, responseWrapper);
        logger.debug("Response capture completed for request: {}", httpRequest.getRequestURI());
    }
}


