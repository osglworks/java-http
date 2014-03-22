package org.osgl.http.result;

import org.osgl._;
import org.osgl.http.Http;

/**
 * Created by luog on 20/03/2014.
 */
public class Unauthorized extends Result {
    private String realm;
    public Unauthorized(String realm) {
        super(Http.Status.BAD_REQUEST);
        this.realm = realm;
    }

    @Override
    public _.T2<String, String> header() {
        return _.T2("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    }
}
