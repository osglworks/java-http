package org.osgl.http.servlet;

import org.osgl.http.H;

import javax.servlet.http.Cookie;

/**
 * Adapt {@link javax.servlet.http.Cookie} to {@link org.osgl.http.H.Cookie}
 */
public class ServletCookie extends H.Cookie {

    private Cookie c;

    public ServletCookie(Cookie cookie) {
        super(cookie.getName());
        c = cookie;
    }

    @Override
    public String name() {
        return c.getName();
    }

    @Override
    public String value() {
        return c.getValue();
    }

    @Override
    public ServletCookie value(String value) {
        c.setValue(value);
        return this;
    }

    @Override
    public String domain() {
        return c.getDomain();
    }

    @Override
    public ServletCookie domain(String domain) {
        c.setDomain(domain);
        return this;
    }

    @Override
    public String path() {
        return c.getPath();
    }

    @Override
    public ServletCookie path(String uri) {
        c.setPath(uri);
        return this;
    }

    @Override
    public long maxAge() {
        return c.getMaxAge();
    }

    @Override
    public ServletCookie maxAge(int maxAge) {
        c.setMaxAge(maxAge);
        return this;
    }

    @Override
    public boolean secure() {
        return c.getSecure();
    }

    @Override
    public ServletCookie secure(boolean secure) {
        c.setSecure(secure);
        return this;
    }

    public static ServletCookie of(Cookie c) {
        return new ServletCookie(c);
    }

    public static Cookie asServletCookie(H.Cookie c) {
        if (c instanceof ServletCookie) {
            return ((ServletCookie)c).c;
        } else {
            Cookie sc = new Cookie(c.name(), c.value());
            sc.setDomain(c.domain());
            sc.setMaxAge((int)c.maxAge());
            sc.setSecure(c.secure());
            sc.setPath(c.path());
            return sc;
        }
    }
}
