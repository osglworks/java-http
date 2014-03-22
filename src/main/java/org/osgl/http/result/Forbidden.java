package org.osgl.http.result;

import org.osgl.http.Http;
import org.osgl.util.S;

/**
 * Created by luog on 20/03/2014.
 */
public class Forbidden extends Result {
    public Forbidden() {
        super(Http.Status.FORBIDDEN);
    }
    public Forbidden(String message) {
        super(Http.Status.FORBIDDEN, message);
    }
    public Forbidden(String message, Object... args) {
        this(S.fmt(message, args));
    }
}
