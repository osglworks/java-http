package org.osgl.http.result;

import org.osgl.http.Http;
import org.osgl.util.S;

/**
 * Created by luog on 20/03/2014.
 */
public class Error extends Result {
    public Error() {
        super(Http.Status.INTERNAL_SERVER_ERROR);
    }
    public Error(String message) {
        super(Http.Status.INTERNAL_SERVER_ERROR, message);
    }
    public Error(String message, Object ... args) {
        this(S.fmt(message, args));
    }
    public Error(Throwable t, String message) {
        super(Http.Status.INTERNAL_SERVER_ERROR, t, message);
    }
    public Error(Throwable t, String message, Object ... args) {
        this(t, S.fmt(message, args));
    }
}
