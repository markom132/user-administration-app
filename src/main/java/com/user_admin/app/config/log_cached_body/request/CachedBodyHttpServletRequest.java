package com.user_admin.app.config.log_cached_body.request;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wrapper for HttpServletRequest to allow caching of the request body, enabling it to be read multiple times.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(CachedBodyHttpServletRequest.class);

    private final byte[] cachedBody;

    /**
     * Constructs a CachedBodyHttpServletRequest, caching the request body for repeated access.
     *
     * @param request the original HttpServletRequest
     * @throws IOException if an input or output exception occurs
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);

        logger.debug("Initializing CachedBodyHttpServletRequest with request body caching.");

        // Read the request body and store it in a byte array
        InputStreamReader inputStreamReader = new InputStreamReader(request.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder body = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            body.append(line);
        }

        cachedBody = body.toString().getBytes();

        logger.debug("Request body cached successfully.");
    }

    /**
     * Returns a ServletInputStream for the cached request body.
     *
     * @return a ServletInputStream for reading the cached request body
     */
    @Override
    public ServletInputStream getInputStream() {
        logger.debug("Providing cached ServletInputStream for request body.");
        return new CachedBodyServletInputStream(cachedBody);
    }

    /**
     * Returns a BufferedReader for the cached request body.
     *
     * @return a BufferedReader for reading the cached request body
     */
    @Override
    public BufferedReader getReader() {
        logger.debug("Providing BufferedReader for cached request body.");
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
