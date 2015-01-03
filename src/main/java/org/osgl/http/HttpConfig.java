package org.osgl.http;

import org.osgl.cache.CacheService;
import org.osgl.cache.CacheServiceProvider;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Locale;

public class HttpConfig {

    private static CacheServiceProvider cacheServiceProvider;

    public static void setCacheServiceProvider(CacheServiceProvider cacheProvider) {
        E.NPE(cacheProvider);
        HttpConfig.cacheServiceProvider = cacheProvider;
    }

    public static CacheService cacheService() {
        if (null == cacheServiceProvider) {
            cacheServiceProvider = CacheServiceProvider.Impl.Auto;
        }
        return cacheServiceProvider.get();
    }

    private static Locale defLocale = Locale.getDefault();

    public static void defaultLocale(Locale locale) {
        E.NPE(locale);
        defLocale = locale;
    }

    public static Locale defaultLocale() {
        return defLocale;
    }

    private static String domain;

    public static void domain(String domain) {
        E.npeIf(S.empty(domain));
        HttpConfig.domain = domain;
    }

    public static String domain() {
        E.illegalStateIf(S.empty(domain), "domain has not been configured");
        return domain;
    }

    private static int port;

    public static void port(int port) {
        E.illegalArgumentIf(port < 1 || port > 65535, "port specified is out of range: %s", port);
        HttpConfig.port = port;
    }

    public static int port() {
        return 0 < port ? port : 80;
    }

    public static int securePort;

    public static void securePort(int port) {
        E.illegalArgumentIf(port < 1 || port > 65535, "port specified is out of range: %s", port);
        HttpConfig.securePort = port;
    }

    public static int securePort() {
        return securePort;
    }

    public static String ctx;

    /**
     * Set url context path. It's either an empty string, or a path
     * starts with "/" but not end with ""
     *
     * @param context the context path to be configured
     */
    public static void contextPath(String context) {
        if (S.empty(context)) ctx = "";
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

    private static String xForwardedAllowed;

    /**
     * Set allowed proxy ips for x-forwarded headers
     * @param proxies the allowed proxy ips
     */
    public static void setXForwardedAllowed(String proxies) {
        xForwardedAllowed = proxies;
    }

    /**
     * Does the app support x-forwarded headers
     * @return
     */
    public static boolean isXForwardedAllowed() {
        return S.notEmpty(xForwardedAllowed);
    }

    /**
     * Does the remote address is allowed for x-forwarded header
     *
     * @param remoteAddr the remote address
     */
    public static boolean isXForwardedAllowed(String remoteAddr) {
        return isXForwardedAllowed() && (S.eq("all", xForwardedAllowed) || xForwardedAllowed.contains(remoteAddr));
    }

    private static boolean cookieSecure;

    /**
     * Returns the cookie secure configuration
     */
    public static boolean isCookieSecure() {
        return cookieSecure;
    }

    /**
     * Configure cookie secure
     *
     * @param secure
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
