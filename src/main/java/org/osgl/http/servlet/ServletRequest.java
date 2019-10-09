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

import org.osgl.$;
import org.osgl.http.H;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

public class ServletRequest extends H.Request {

    private H.Method methodOverride;

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
    @SuppressWarnings("unchecked")
    public Iterable<String> headerNames() {
        return C.enumerable(r.getHeaderNames());
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
    protected String _ip() {
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
        return null == methodOverride ? H.Method.valueOfIgnoreCase(r.getMethod()) : methodOverride;
    }

    @Override
    public ServletRequest method(H.Method method) {
        this.methodOverride = $.NPE(method);
        return this;
    }

    @Override
    public InputStream createInputStream() throws IllegalStateException {
        try {
            return r.getInputStream();
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
