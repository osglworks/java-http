package org.osgl.http;

/*-
 * #%L
 * OSGL HTTP
 * %%
 * Copyright (C) 2017 - 2018 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public interface CurrentStateStore {

    /**
     * Get current request.
     * @return current request
     */
    H.Request request();

    /**
     * Get current response.
     * @return current response.
     */
    H.Response response();

    /**
     * Get current session.
     * @return current session.
     */
    H.Session session();

    /**
     * Get current flash.
     * @return current flash.
     */
    H.Flash flash();

    /**
     * Store session as current state.
     *
     * @param sess
     *      the session object
     */
    void session(H.Session sess);

    /**
     * Store request as current state.
     *
     * @param req
     *      the request object
     */
    void request(H.Request req);

    /**
     * Store response as current state.
     *
     * @param resp
     *      the response object
     */
    void response(H.Response resp);

    /**
     * Store flash as current state.
     *
     * @param flash
     *      the flash object
     */
    void flash(H.Flash flash);

    /**
     * Clear all current states
     */
    void clear();
}
