package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * A HttpServletResponse wrapper that caches the response body,
 * allowing it to be accessed multiple times.
 */
public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private static final Logger logger = LoggerFactory.getLogger(CachedBodyHttpServletResponse.class);

    private final ByteArrayOutputStream cachedContent = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;

    /**
     * Construct a CachedBodyHttpServletResponse to enable response caching.
     *
     * @param response the original HttpServletResponse to be wrapped
     * @throws IOException if an I/O error occurs while initializing the output stream
     */
    public CachedBodyHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        logger.debug("CachedBodyHttpServletResponse initialized for response caching.");
    }

    /**
     * Retrieves the ServletOutputStream and caches the written data,
     *
     * @return the ServletOutputStream for the response
     * @throws IOException if an I/O error occurs while obtaining the output stream
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new TeeServletOutputStream(super.getOutputStream(), cachedContent);
            logger.debug("ServletOutputStream wrapped with caching output stream.");
        }
        return outputStream;
    }

    /**
     * Flushes the output stream buffer to ensure all data is written.
     *
     * @throws IOException if an I/O error occurs during flushing
     */
    @Override
    public void flushBuffer() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
            logger.debug("Output stream buffer flushed.");
        }
    }

    /**
     * Returns the cached response content as a byte array.
     *
     * @return byte array of cached content
     */
    public byte[] getCachedContent() {
        logger.debug("Cached content retrieved with size: {} bytes.", cachedContent.size());
        return cachedContent.toByteArray();
    }
}
