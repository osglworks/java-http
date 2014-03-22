package org.osgl.http;

/**
 * Stores the Context Local variables
 */
final class Current {
    private static ThreadLocal<H.Session> _sess = new ThreadLocal<H.Session>();
    private static ThreadLocal<H.Request> _req = new ThreadLocal<H.Request>();
    private static ThreadLocal<H.Response> _resp = new ThreadLocal<H.Response>();
    private static ThreadLocal<H.Flash> _flash = new ThreadLocal<H.Flash>();

    static H.Session session() {
        return _sess.get();
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

    static void clear() {
        _sess.remove();
        _req.remove();
        _resp.remove();
    }
}
