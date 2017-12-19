package org.osgl.http.util;

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

import org.osgl.http.H;
import org.osgl.http.HttpConfig;
import org.osgl.util.E;
import org.osgl.util.FastStr;
import org.osgl.util.ListBuilder;
import org.osgl.util.S;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

        String ctx = req.contextPath();
        StringBuilder sb = S.builder();
        if (!"/".equals(ctx)) {
            sb.append(ctx);
        }
        if (!path.startsWith("/")) sb.append("/");
        return sb.append(path).toString();
    }

    /**
     * Returns the full url form of a url path which starts
     * with scheme. The scheme, domain, port, context path
     * info are get from {@link HttpConfig}
     *
     * @param path the url path
     * @return the full url of the path
     */
    public static String fullUrl(String path) {
        return fullUrl(path, HttpConfig.secure());
    }

    /**
     * Return the full url form of a url path which starts with
     * scheme. The domain, port and context path info are get from
     * {@link HttpConfig}
     *
     * @param path the path to be normalized
     * @param secure is the full url needs to be secure or no
     * @return the full url of the path
     */
    public static String fullUrl(String path, boolean secure) {
        if (isFullUrl(path)) return path;
        StringBuilder sb = S.builder(secure ? "https://" : "http://");
        sb.append(HttpConfig.domain());
        if (secure) {
            if (443 != HttpConfig.securePort()) {
                sb.append(":").append(HttpConfig.securePort());
            }
        } else {
            if (80 != HttpConfig.nonSecurePort()) {
                sb.append(":").append(HttpConfig.nonSecurePort());
            }
        }
        if (!path.startsWith("//")) {
            String ctx = HttpConfig.contextPath();
            if (S.notBlank(ctx) && !"/".equals(ctx)) {
                sb.append(ctx);
            }
        }
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
        boolean secure = req.scheme().equals("https");
        StringBuilder sb = S.builder(req.scheme()).append("://");
        sb.append(req.domain());
        int reqPort = req.port();
        if (secure) {
            if (443 != reqPort) {
                sb.append(":").append(reqPort);
            }
        } else {
            if (80 != reqPort) {
                sb.append(":").append(reqPort);
            }
        }
        if (!path.startsWith("//")) {
            String ctx = req.contextPath();
            if (!"/".equals(ctx)) {
                sb.append(req.contextPath());
            }
        }
        if (!path.startsWith("/")) sb.append("/");
        sb.append(path);
        return sb.toString();
    }

    public static Iterator<CharSequence> tokenizer(final char[] buf) {
        return tokenizer(buf, 0);
    }

    public static Iterator<CharSequence> tokenizer(final char[] buf, final int start) {
        return tokenizer(buf, start, '/', '?');
    }

    public static Iterator<CharSequence> tokenizer(final char[] buf, final int start, final char separator, final char terminator) {
        return new Iterator<CharSequence>() {
            int cursor = start;
            final int len = buf.length;
            int begin = -1;
            public boolean hasNext() {
                // filter out separator
                while (cursor < len) {
                    char c = buf[cursor];
                    if (c == separator) {
                        cursor++;
                    } else {
                        break;
                    }
                }
                return cursor < len && buf[cursor] != terminator;
            }

            @Override
            public CharSequence next() {
                for (int i = cursor; i < len; ++i) {
                    char c = buf[i];
                    if (c == terminator) {
                        cursor = len;
                        return (FastStr.unsafeOf(buf, begin, i));
                    }
                    if (c == separator) {
                        if (begin > -1) {
                            CharSequence ret = (FastStr.unsafeOf(buf, begin, i));
                            cursor = i + 1;
                            begin = -1;
                            return ret;
                        }
                    } else if (begin == -1) {
                        begin = i;
                    }
                    if (i == len - 1 && begin != -1) {
                        // the last one
                        cursor = len;
                        return(FastStr.unsafeOf(buf, begin, i + 1));
                    }
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw E.unsupport();
            }
        };
    }

    public static List<CharSequence> tokenize(char[] buf) {
        return tokenize(buf, 0);
    }

    public static List<CharSequence> tokenize(char[] buf, int start) {
        return tokenize(buf, start, '/', '?');
    }

    public static List<CharSequence> tokenize(char[] buf, int start, char separator, char terminator) {
        int len = buf.length;
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
