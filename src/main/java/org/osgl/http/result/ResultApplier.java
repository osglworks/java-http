package org.osgl.http.result;

import org.osgl._;

/**
 * Provides interface to apply {@link org.osgl.http.result.Result}
 */
public interface ResultApplier {

    /**
     * Returns a function that apply the status code to response
     *
     * @return status applier
     */
    _.F0<Integer> statusApplier();
}
