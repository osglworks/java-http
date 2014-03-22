package org.osgl.http.result;

import org.osgl._;
import org.osgl.exception.FastRuntimeException;
import org.osgl.http.Http;

/**
 * Created by luog on 16/01/14.
 */
public class Result extends FastRuntimeException {

    private final Http.Status status;

    protected Result() {status = null;}

    protected Result(Http.Status status) {
        this.status = status;
    }

    protected Result(Http.Status status, String message) {
        super(message);
        this.status = status;
    }

    protected Result(Http.Status status, String message, Object... args) {
        super(message, args);
        this.status = status;
    }

    protected Result(Http.Status status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    protected Result(Http.Status status, Throwable cause, String message, Object... args) {
        super(cause, message, args);
        this.status = status;
    }

    public Http.Status status() {
        return status;
    }

    public int statusCode() {
        return status().code();
    }

    public _.T2<String, String> header() {
        return null;
    }

}
