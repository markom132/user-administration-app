package com.user_admin.app.config.log_cached_body.response;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;

public class CachedBodyServletOutputStream extends ServletOutputStream {
    private final ByteArrayOutputStream outputStream;

    public CachedBodyServletOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) {
        outputStream.write(b);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        throw new UnsupportedOperationException();
    }
}


