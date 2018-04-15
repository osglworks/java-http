package org.osgl.http.util;

/*-
 * #%L
 * OSGL HTTP
 * %%
 * Copyright (C) 2017 - 2018 OSGL (Open Source General Library)
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
import org.osgl.http.CurrentStateStore;
import org.osgl.http.H;

public class DefaultCurrentStateStore implements CurrentStateStore {

    static ContextLocal<H.Session> _sess = $.contextLocal();
    static ContextLocal<H.Request> _req = $.contextLocal();
    static ContextLocal<H.Response> _resp = $.contextLocal();
    static ContextLocal<H.Flash> _flash = $.contextLocal();

    @Override
    public H.Request request() {
        return _req.get();
    }

    @Override
    public H.Response response() {
        return _resp.get();
    }

    @Override
    public H.Session session() {
        return _sess.get();
    }

    @Override
    public H.Flash flash() {
        return _flash.get();
    }

    @Override
    public void session(H.Session sess) {
        _sess.set(sess);
    }

    @Override
    public void request(H.Request req) {
        _req.set(req);
    }

    @Override
    public void response(H.Response resp) {
        _resp.set(resp);
    }

    @Override
    public void flash(H.Flash flash) {
        _flash.set(flash);
    }

    @Override
    public void clear() {
        _sess.remove();
        _req.remove();
        _resp.remove();
        _flash.remove();
    }
}
