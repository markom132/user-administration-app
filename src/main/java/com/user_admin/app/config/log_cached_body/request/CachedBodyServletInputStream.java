package com.user_admin.app.config.log_cached_body.request;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Custom ServletInputStream that wraps a cached body, allowing multiple reads of the request body.
 */
public class CachedBodyServletInputStream extends ServletInputStream {

    private static final Logger logger = LoggerFactory.getLogger(CachedBodyServletInputStream.class);

    private final ByteArrayInputStream inputStream;

    /**
     * Initializes a CachedBodyServletInputStream with the cached request body.
     *
     * @param cachedBody byte array containing the cached request body
     */
    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.inputStream = new ByteArrayInputStream(cachedBody);
        logger.debug("CachedBodyServletInputStream initialized with cached request body.");
    }

    /**
     * Checks if the input stream has been fully read.
     *
     * @return true if the input stream has no more data, false otherwise
     */
    @Override
    public boolean isFinished() {
        boolean finished = inputStream.available() == 0;
        logger.debug("isFinished called. Result: {}", finished);
        return finished;
    }

    /**
     * Indicates readiness to read from the stream.
     *
     * @return true, as this stream is always ready to be read
     */
    @Override
    public boolean isReady() {
        logger.debug("isReady called. Result: true");
        return true;
    }

    /**
     * Unsupported operation, as asynchronous reading is not implemented.
     *
     * @param readListener The non-blocking IO read listener
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public void setReadListener(ReadListener readListener) {
        logger.warn("setReadListener called, but operation is not supported.");
        throw new UnsupportedOperationException("Non-blocking read is not supported.");
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or -1 if the end of stream is reached
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        int byteData = inputStream.read();
        logger.debug("read called, Byte read: {}", byteData);
        return byteData;
    }
}
