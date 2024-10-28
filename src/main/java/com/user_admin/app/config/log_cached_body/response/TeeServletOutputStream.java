package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.ServletOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A custom ServletOutputStream that duplicates (or "tees") the output stream.
 * This allows capturing the response body by writing to both the original response
 * output stream and a secondary copy stream simultaneously.
 */
public class TeeServletOutputStream extends ServletOutputStream {

    private static final Logger logger = LoggerFactory.getLogger(TeeServletOutputStream.class);

    private final ServletOutputStream outputStream;
    private final ByteArrayOutputStream copyStream;

    /**
     * Constructs a TeeServletOutputStream with the given output and copy streams.
     *
     * @param outputStream the original response output stream to write to
     * @param copyStream   a ByteArrayOutputStream to capture a copy of the written data
     */
    public TeeServletOutputStream(ServletOutputStream outputStream, ByteArrayOutputStream copyStream) {
        this.outputStream = outputStream;
        this.copyStream = copyStream;
        logger.debug("TeeServletOutputStream initialized with output and copy streams.");
    }

    /**
     * Writes a byte to both the original output stream and the copy stream.
     *
     * @param b the byte to be written {@code byte}.
     * @throws IOException if an I/O error occurs while writing to either stream
     */
    @Override
    public void write(int b) throws IOException {
        outputStream.write(b); // Write to the original response stream
        copyStream.write(b); // Write to the copy stream for capturing response data
        logger.trace("Byte written to both output and copy streams.");
    }

    /**
     * Checks if the output stream is ready to be written to.
     *
     * @return true if the output stream is ready, false otherwise
     */
    @Override
    public boolean isReady() {
        return outputStream.isReady();
    }

    /**
     * Sets a WriteListener to observe non-blocking writes to the output stream.
     *
     * @param writeListener the WriteListener to set on the output stream
     */
    @Override
    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
        outputStream.setWriteListener(writeListener);
        logger.debug("WriteListener set for output stream.");
    }
}
