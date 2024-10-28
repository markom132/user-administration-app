package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * Custom ServletOutputStream that writes data to a ByteArrayOutputStream,
 * allowing the response body to be cached and reused.
 */
public class CachedBodyServletOutputStream extends ServletOutputStream {

    private static final Logger logger = LoggerFactory.getLogger(CachedBodyServletOutputStream.class);

    private final ByteArrayOutputStream outputStream;

    /**
     * Constructs a CachedBodyServletOutputStream for caching response content.
     *
     * @param outputStream the ByteArrayOutputStream to cache response content
     */
    public CachedBodyServletOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
        logger.debug("CachedBodyServletOutputStream initialized for response caching.");
    }

    /**
     * Writes a byte of data to the output stream and caches it.
     *
     * @param b the byte of data to be written {@code byte}.
     */
    @Override
    public void write(int b) {
        outputStream.write(b);
        logger.trace("Byte written to cached output stream: {}", b);
    }

    /**
     * Indicates that the stream is ready for writing at any time.
     *
     * @return true, as the stream is always ready for writing
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * This method is not supported in CachedBodyServletOutputStream.
     *
     * @param listener Ta WriteListener to be notified when the stream is ready
     * @throws UnsupportedOperationException because setting a WriteListener is not supported
     */
    @Override
    public void setWriteListener(WriteListener listener) {
        throw new UnsupportedOperationException("WriteListener not supported in CachedBodyServletOutputStream.");
    }
}


