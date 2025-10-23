package com.hackathon.sdk.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Wrapper to cache the response body while still sending it to the client.
 */
public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream;
    private final PrintWriter writer;

    public CachedBodyHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        this.outputStream = new CachedBodyServletOutputStream(cachedBody, response.getOutputStream());
        this.writer = new PrintWriter(new OutputStreamWriter(cachedBody, response.getCharacterEncoding()));
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    public byte[] getCachedBody() {
        return cachedBody.toByteArray();
    }

    private static class CachedBodyServletOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream cachedBody;
        private final ServletOutputStream delegate;

        public CachedBodyServletOutputStream(ByteArrayOutputStream cachedBody, ServletOutputStream delegate) {
            this.cachedBody = cachedBody;
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            cachedBody.write(b);
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            delegate.setWriteListener(writeListener);
        }
    }
}