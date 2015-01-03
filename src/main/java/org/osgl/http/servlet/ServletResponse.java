package org.osgl.http.servlet;

import org.osgl.exception.UnexpectedIOException;
import org.osgl.http.H;
import org.osgl.util.E;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Locale;

public class ServletResponse extends H.Response<ServletResponse> {
    @Override
    protected Class<ServletResponse> _impl() {
        return ServletResponse.class;
    }

    private HttpServletResponse r;

    public ServletResponse(HttpServletResponse resp) {
        E.NPE(resp);
        r = resp;
    }

    @Override
    public String characterEncoding() {
        return r.getCharacterEncoding();
    }

    @Override
    public ServletResponse characterEncoding(String encoding) {
        r.setCharacterEncoding(encoding);
        return this;
    }

    @Override
    public OutputStream outputStream() throws IllegalStateException, UnexpectedIOException {
        try {
            return r.getOutputStream();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    public Writer writer() throws IllegalStateException, UnexpectedIOException {
        try {
            return r.getWriter();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    public ServletResponse contentLength(int len) {
        r.setContentLength(len);
        return this;
    }

    @Override
    protected void _setContentType(String type) {
        r.setContentType(type);
    }

    @Override
    protected void _setLocale(Locale loc) {
        r.setLocale(loc);
    }

    @Override
    public Locale locale() {
        return r.getLocale();
    }

    @Override
    public void addCookie(H.Cookie cookie) {
        r.addCookie(ServletCookie.asServletCookie(cookie));
    }

    @Override
    public boolean containsHeader(String name) {
        return r.containsHeader(name);
    }

    @Override
    public ServletResponse sendError(int sc, String msg) {
        try {
            r.sendError(sc, msg);
        } catch (IOException e) {
            throw E.ioException(e);
        }
        return this;
    }

    @Override
    public ServletResponse sendError(int sc) {
        try {
            r.sendError(sc);
        } catch (IOException e) {
            throw E.ioException(e);
        }
        return this;
    }

    @Override
    public ServletResponse sendRedirect(String location) {
        try {
            r.sendRedirect(location);
        } catch (IOException e) {
            throw E.ioException(e);
        }
        return this;
    }

    @Override
    public ServletResponse header(String name, String value) {
        r.setHeader(name, value);
        return this;
    }

    @Override
    public ServletResponse status(int sc) {
        r.setStatus(sc);
        return this;
    }

    @Override
    public ServletResponse addHeader(String name, String value) {
        r.addHeader(name, value);
        return this;
    }
}
