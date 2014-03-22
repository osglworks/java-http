package org.osgl.http.result;

import org.osgl._;
import org.osgl.http.Http;
import org.osgl.util.E;
import org.osgl.util.S;

/**
 * Created by luog on 20/03/2014.
 */
public class Redirect extends Result {
    String url;
    public Redirect(String url) {
        super(Http.Status.FOUND);
        E.illegalArgumentIf(S.empty(url));
        this.url = url;
    }

    public Redirect(String url, Object... args) {
        this(S.fmt(url, args));
    }

    public Redirect(boolean permanent, String url) {
        super(Http.Status.MOVED_PERMANENTLY);
        this.url = url;
    }

    @Override
    public _.T2<String, String> header() {
        return null;
    }
}
