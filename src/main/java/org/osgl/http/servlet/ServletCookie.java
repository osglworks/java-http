package org.osgl.http.servlet;

/*-
 * #%L
 * OSGL HTTP
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    public int maxAge() {
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
