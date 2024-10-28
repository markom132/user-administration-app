package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.ServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TeeServletOutputStream extends ServletOutputStream {
    private final ServletOutputStream outputStream;
    private final ByteArrayOutputStream copyStream;

    public TeeServletOutputStream(ServletOutputStream outputStream, ByteArrayOutputStream copyStream) {
        this.outputStream = outputStream;
        this.copyStream = copyStream;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        copyStream.write(b);
    }

    @Override
    public boolean isReady() {
        return outputStream.isReady();
    }

    @Override
    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
        outputStream.setWriteListener(writeListener);
    }
}
