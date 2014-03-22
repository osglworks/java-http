package org.osgl.http.result;

import org.osgl.http.Http;
import org.osgl.util.S;

/**
 * Created by luog on 20/03/2014.
 */
public class NotFound extends Result {

    public NotFound() {
        super(Http.Status.NOT_FOUND);
    }

    public NotFound(String message) {
        super(Http.Status.NOT_FOUND, message);
    }

    public NotFound(String message, Object... args) {
        this(S.fmt(message, args));
    }
}
