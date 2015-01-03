package org.osgl.http.util;

import org.osgl.http.H;
import org.osgl.http.HttpConfig;
import org.osgl.util.FastStr;
import org.osgl.util.ListBuilder;
import org.osgl.util.S;

import java.util.List;

public enum Path {
    ;

    /**
     * Check if a path is full URL including scheme
     *
     * @param path the path to be checked
     * @return {@code true} if the path is full url
     */
    public static boolean isFullUrl(String path) {
        return path.startsWith("http:") || path.startsWith("https:");
    }

    /**
     * Check if a path is absolute. A path is considered to be
     * absolute if it is {@link #isFullUrl(String) full url} or
     * it starts with {@code "//"}
     *
     * @param path the path to be checked
     * @return {@code true} if the path is absolute
     */
    public static boolean isAbsoluteUrl(String path) {
        return isFullUrl(path) || path.startsWith("//");
    }

    /**
     * Returns a normalized url for a path. If the path
     * is {@link #isAbsoluteUrl(String) absolute} then
     * it is returned directly.
     *
     * <p>
     * Otherwise the path is prepended with the
     * {@link org.osgl.http.HttpConfig#contextPath() context path}
     * </p>
     *
     * @param path the url path to be normalized
     * @return the normalized url
     */
    public static String url(String path) {
        if (isAbsoluteUrl(path)) return path;
        StringBuilder sb = S.builder(HttpConfig.contextPath());
        if (!path.startsWith("/")) sb.append("/");
        return sb.append(path).toString();
    }

    /**
     * Returns normalized form of URL path. If the path is
     * {@link #isAbsoluteUrl(String) absolute} then it is
     * returned directly
     *
     * <p>
     * Otherwise the path is prepended with the
     * {@link org.osgl.http.H.Request#contextPath() context} info
     * </p>
     *
     * <p>If the request is {@code null} then
     * {@link #url(String)} is called</p>
     *
     * @param path the url path to be normalized
     * @param req the request instance
     * @return full path of the URL specified
     */
    public static String url(String path, H.Request req) {
        if (null == req) return url(path);
        if (isAbsoluteUrl(path)) return path;

        StringBuilder sb = S.builder(req.contextPath());
        if (!path.startsWith("/")) sb.append("/");
        return sb.append(path).toString();
    }

    /**
     * Return the full url form of a url path which starts with
     * scheme. The domain, port and context path info are get from
     * {@link org.osgl.http.HttpConfig}
     *
     * @param path the path to be normalized
     * @param secure is the full url needs to be secure or no
     * @return the full url of the path
     */
    public static String fullUrl(String path, boolean secure) {
        if (isFullUrl(path)) return path;
        StringBuilder sb = S.builder(secure ? "https://" : "http://");
        sb.append(HttpConfig.domain()).append(":").append(HttpConfig.port());
        if (!path.startsWith("//")) sb.append(HttpConfig.contextPath());
        if (!path.startsWith("/")) sb.append("/");
        sb.append(path);
        return sb.toString();
    }

    /**
     * Returns the full url form of a url path which starts with
     * scheme. the domain, port and context path info are get
     * from the {@link org.osgl.http.H.Request} specified. If the
     * request is {@code null} then it will call back {@link #fullUrl(String, boolean)}
     * with {@code secure} be set to {@code false}.
     *
     * @param path the path to be normalized
     * @param req the http request from which the domain, port
     *          and context path are fetched
     * @return the full url of the path
     */
    public static String fullUrl(String path, H.Request req) {
        if (null == req) return fullUrl(path, false);

        if (isFullUrl(path)) return path;
        StringBuilder sb = S.builder(req.scheme()).append("://");
        sb.append(req.domain()).append(":").append(req.port());
        if (!path.startsWith("//")) sb.append(req.contextPath());
        if (!path.startsWith("/")) sb.append("/");
        sb.append(path);
        return sb.toString();
    }

    public static List<CharSequence> tokenize(char[] buf) {
        return tokenize(buf, 0);
    }

    public static List<CharSequence> tokenize(char[] buf, int start) {
        return tokenize(buf, start, '/', '?');
    }

    public static List<CharSequence> tokenize(char[] buf, int start, char separator, char terminator) {
        int len = buf.length - start;
        ListBuilder<CharSequence> lb = ListBuilder.create();
        int begin = -1;
        for (int i = start; i < len; ++i) {
            char c = buf[i];
            if (c == terminator) {
                lb.add(FastStr.unsafeOf(buf, begin, i));
                break;
            }
            if (c == separator) {
                if (begin > -1) {
                    lb.add(FastStr.unsafeOf(buf, begin, i));
                }
                begin = -1;
            } else if (begin == -1) {
                begin = i;
            }
            if (i == len - 1 && begin != -1) {
                // the last one
                lb.add(FastStr.unsafeOf(buf, begin, i + 1));
            }
        }
        return lb.toList();
    }

}
