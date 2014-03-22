package org.osgl.http.result;

import org.osgl._;
import org.osgl.http.Http;
import org.osgl.util.S;

/**
 * Created by luog on 20/03/2014.
 */
public class NotModified extends Result {
    String etag;
    public NotModified() {
        super(Http.Status.NOT_MODIFIED);
    }
    public NotModified(String etag) {
        super(Http.Status.NOT_MODIFIED);
        this.etag = etag;
    }
    public NotModified(String etag, Object... args) {
        this(S.fmt(etag, args));
    }

    @Override
    public _.T2<String, String> header() {
        return null == etag ? null : _.T2("Etag", etag);
    }
}
