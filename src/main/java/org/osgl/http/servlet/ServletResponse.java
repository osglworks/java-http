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
import org.osgl.util.E;
import org.osgl.util.Output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

public class ServletResponse extends H.Response<ServletResponse> {
    @Override
    protected Class<ServletResponse> _impl() {
        return ServletResponse.class;
    }

    private HttpServletResponse r;
    private int statusCode = -1;

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
    public ServletResponse contentLength(long len) {
        r.setContentLength((int) len);
        return this;
    }

    /**
     * This method is not supported in ServletResponse
     * @param buffer direct byte buffer
     * @return this response instance
     */
    @Override
    public ServletResponse writeContent(ByteBuffer buffer) {
        throw E.unsupport("Writing direct byte buffer is not supported by ServletResponse");
    }

    @Override
    protected OutputStream createOutputStream() {
        try {
            return r.getOutputStream();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    protected Output createOutput() {
        try {
            return Output.Adaptors.of(r.getOutputStream());
        } catch (IOException e) {
            throw E.ioException(e);
        }
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
    public ServletResponse sendError(int statusCode, String msg) {
        try {
            r.sendError(statusCode, msg);
        } catch (IOException e) {
            throw E.ioException(e);
        }
        return this;
    }

    @Override
    public ServletResponse sendError(int statusCode) {
        try {
            r.sendError(statusCode);
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
    public ServletResponse status(int statusCode) {
        r.setStatus(statusCode);
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public ServletResponse addHeader(String name, String value) {
        r.addHeader(name, value);
        return this;
    }

    @Override
    public void commit() {
        try {
            r.flushBuffer();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }
}
