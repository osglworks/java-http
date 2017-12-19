package org.osgl.http;

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
import org.osgl.concurrent.ContextLocal;

import java.util.Collection;
import java.util.Map;

/**
 * Stores the Context Local variables
 */
final class Current {
    static ContextLocal<H.Session> _sess = $.contextLocal();
    static ContextLocal<H.Request> _req = $.contextLocal();
    static ContextLocal<H.Response> _resp = $.contextLocal();
    static ContextLocal<H.Flash> _flash = $.contextLocal();
    static ContextLocal<String> _fmt = $.contextLocal();
    static ContextLocal<Map<String, H.Cookie>> _cookies = $.contextLocal();

    static H.Session session() {
        return _sess.get();
    }

    static boolean cookieMapInitialized() {
        return null != _cookies.get();
    }

    static void setCookie(String name, H.Cookie cookie) {
        _cookies.get().put(name, cookie);
    }

    static H.Cookie getCookie(String name) {
        return _cookies.get().get(name);
    }

    static Collection<H.Cookie> cookies() {
        return _cookies.get().values();
    }

    static H.Flash flash() {
        return _flash.get();
    }

    static H.Request request() {
        return _req.get();
    }

    static H.Response response() {
        return _resp.get();
    }

    static String format() {
        return _fmt.get();
    }

    static void session(H.Session sess) {
        _sess.set(sess);
    }

    static void request(H.Request req) {
        _req.set(req);
    }

    static void response(H.Response resp) {
        _resp.set(resp);
    }

    static void flash(H.Flash flash) {
        _flash.set(flash);
    }

    static void format(String fmt) {
        _fmt.set(fmt);
    }

    static void clear() {
        _sess.remove();
        _req.remove();
        _resp.remove();
        _fmt.remove();
        _cookies.remove();
    }
}
