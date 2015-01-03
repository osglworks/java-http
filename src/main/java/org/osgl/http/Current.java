package org.osgl.http;

import org.osgl._;
import org.osgl.concurrent.ContextLocal;

import java.util.Collection;
import java.util.Map;

/**
 * Stores the Context Local variables
 */
final class Current {
    static ContextLocal<H.Session> _sess = _.contextLocal();
    static ContextLocal<H.Request> _req = _.contextLocal();
    static ContextLocal<H.Response> _resp = _.contextLocal();
    static ContextLocal<H.Flash> _flash = _.contextLocal();
    static ContextLocal<String> _fmt = _.contextLocal();
    static ContextLocal<Map<String, H.Cookie>> _cookies = _.contextLocal();

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
