package org.osgl.http;

/*-
 * #%L
 * OSGL HTTP
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
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

import org.osgl.$;
import org.osgl.cache.CacheService;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Locale;

public class HttpConfig {

    private static CacheService sessionCache;

    public static void setSessionCache(CacheService cache) {
        sessionCache = $.requireNotNull(cache);
    }

    public static CacheService sessionCache() {
        return sessionCache;
    }

    private static Locale defLocale = Locale.getDefault();

    public static void defaultLocale(Locale locale) {
        E.NPE(locale);
        defLocale = locale;
    }

    public static Locale defaultLocale() {
        return defLocale;
    }

    public static void setCurrentStateStore(CurrentStateStore currentStateStore) {
        H.setCurrentStateStore(currentStateStore);
    }

    private static String domain;

    public static void domain(String domain) {
        E.npeIf(S.blank(domain));
        HttpConfig.domain = domain;
    }

    public static String domain() {
        E.illegalStateIf(S.blank(domain), "domain has not been configured");
        return domain;
    }

    private static int nonSecurePort;

    public static void nonSecurePort(int port) {
        E.illegalArgumentIf(port < 1 || port > 65535, "port specified is out of range: %s", port);
        HttpConfig.nonSecurePort = port;
    }

    public static int nonSecurePort() {
        return 0 < nonSecurePort ? nonSecurePort : 80;
    }

    public static int port() {
        return secure ? securePort : nonSecurePort;
    }

    public static int securePort;

    public static void securePort(int port) {
        E.illegalArgumentIf(port < 1 || port > 65535, "port specified is out of range: %s", port);
        HttpConfig.securePort = port;
    }

    public static int securePort() {
        return securePort;
    }

    public static boolean secure;

    public static void secure(boolean setting) {
        HttpConfig.secure = setting;
    }

    public static boolean secure() {
        return HttpConfig.secure;
    }

    public static String ctx;

    /**
     * Set url context path. It's either an empty string, or a path
     * starts with "/" but not end with ""
     *
     * @param context the context path to be configured
     */
    public static void contextPath(String context) {
        if (S.blank(context)) ctx = "";
        else {
            E.illegalArgumentIf(!context.startsWith("/"), "context path should start with \"/\"");
            if (context.endsWith("/")) {
                ctx = S.str(context).beforeLast("/").toString();
            }
        }
    }

    public static String contextPath() {
        return ctx;
    }

    private static String xForwardedAllowed = "all";

    /**
     * Set allowed proxy ips for x-forwarded headers
     * @param allowed the allowed proxy ips
     */
    public static void setXForwardedAllowed(String allowed) {
        xForwardedAllowed = allowed;
    }

    /**
     * Does the app support x-forwarded headers
     * @return is x-forwarded header supported
     */
    public static boolean isXForwardedAllowed() {
        return S.notBlank(xForwardedAllowed);
    }

    /**
     * Does the remote address is allowed for x-forwarded header
     *
     * @param remoteAddr the remote address
     * @return is the remote address allowed for x-forwarded header
     */
    public static boolean isXForwardedAllowed(String remoteAddr) {
        return isXForwardedAllowed() && (S.eq("all", xForwardedAllowed) || xForwardedAllowed.contains(remoteAddr));
    }

    private static boolean extensiveRemoteAddrResolving = false;

    public static void setExtensiveRemoteAddrResolving(boolean setting) {
        extensiveRemoteAddrResolving = setting;
    }

    public static boolean allowExtensiveRemoteAddrResolving() {
        return extensiveRemoteAddrResolving;
    }

    private static boolean cookieSecure;

    /**
     * Returns the cookie secure configuration
     * @return the cookie secure setting
     */
    public static boolean isCookieSecure() {
        return cookieSecure;
    }

    /**
     * Configure cookie secure
     *
     * @param secure cookie secure
     */
    public static void setCookieSecure(boolean secure) {
        cookieSecure = secure;
    }

    private static boolean cookieHttpOnly;

    public static boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }

    public static void setCookieHttpOnly(boolean httpOnly) {
        cookieHttpOnly = httpOnly;
    }

}
