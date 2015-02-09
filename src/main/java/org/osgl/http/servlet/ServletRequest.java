package org.osgl.http.servlet;

import org.osgl.http.H;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ServletRequest extends H.Request {
    @Override
    protected Class _impl() {
        return ServletRequest.class;
    }

    private HttpServletRequest r;

    public ServletRequest(HttpServletRequest req) {
        E.NPE(req);
        r = req;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<String> headers(String name) {
        return C.enumerable(r.getHeaders(name));
    }

    @Override
    public String path() {
        String s1 = r.getServletPath();
        String s2 = r.getPathInfo();
        if (null == s1) return s2;
        if (null == s2) return s1;

        return s1 + s2;
    }

    @Override
    public String contextPath() {
        return r.getContextPath();
    }

    @Override
    public String query() {
        return r.getQueryString();
    }

    @Override
    public boolean secure() {
        return r.isSecure();
    }

    @Override
    protected String _remoteAddr() {
        return r.getRemoteAddr();
    }

    private C.Map<String, H.Cookie> cookies;

    @Override
    protected void _initCookieMap() {
        Cookie[] ca = r.getCookies();
        for (Cookie c : ca) {
            _setCookie(c.getName(), ServletCookie.of(c));
        }
    }

    @Override
    public String header(String name) {
        return r.getHeader(name);
    }

    @Override
    public H.Method method() {
        return H.Method.valueOfIgnoreCase(r.getMethod());
    }

    @Override
    public InputStream inputStream() throws IllegalStateException {
        try {
            return r.getInputStream();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    public Reader reader() throws IllegalStateException {
        try {
            return r.getReader();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    public String paramVal(String name) {
        return r.getParameter(name);
    }

    @Override
    public String[] paramVals(String name) {
        String[] ret = r.getParameterValues(name);
        if (null == ret) {
            ret = new String[0];
        }
        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<String> paramNames() {
        return C.enumerable(r.getParameterNames());
    }
}
