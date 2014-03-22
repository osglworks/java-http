package org.osgl.http;

import org.osgl._;
import org.osgl.cache.CacheService;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.*;

import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static org.osgl.http.H.Header.Names.*;

/**
 * The namespace to access Http features
 */
public class H {

    protected static final Logger logger = L.get(Http.class);

    public static enum Method {
        GET, HEAD, POST, DELETE, PUT, PATCH, TRACE, OPTIONS, CONNECT;

        private String id;

        private Method() {
            id = name().intern();
        }

        private static EnumSet<Method> unsafeMethods = EnumSet.of(POST, DELETE, PUT, PATCH);

        /**
         * Returns if this http method is safe, meaning it
         * won't change the state of the server
         *
         * @see #unsafe()
         */
        public boolean safe() {
            return !unsafe();
        }

        /**
         * Returns if this http method is unsafe, meaning
         * it will change the state of the server
         *
         * @see #safe()
         */
        public boolean unsafe() {
            return unsafeMethods.contains(this);
        }
    } // eof Method

    /**
     * The HTTP Status constants
     */
    public static enum Status {

        // 1xx Informational

        /**
         * {@code 100 Continue}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.1.1">HTTP/1.1</a>
         */
        CONTINUE(100),
        /**
         * {@code 101 Switching Protocols}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.1.2">HTTP/1.1</a>
         */
        SWITCHING_PROTOCOLS(101),
        /**
         * {@code 102 Processing}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
         */
        PROCESSING(102),
        /**
         * {@code 103 Checkpoint}.
         *
         * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
         * resumable POST/PUT HTTP requests in HTTP/1.0</a>
         */
        CHECKPOINT(103),

        // 2xx Success

        /**
         * {@code 200 OK}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.1">HTTP/1.1</a>
         */
        OK(200),
        /**
         * {@code 201 Created}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.2">HTTP/1.1</a>
         */
        CREATED(201),
        /**
         * {@code 202 Accepted}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.3">HTTP/1.1</a>
         */
        ACCEPTED(202),
        /**
         * {@code 203 Non-Authoritative Information}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.4">HTTP/1.1</a>
         */
        NON_AUTHORITATIVE_INFORMATION(203),
        /**
         * {@code 204 No Content}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.5">HTTP/1.1</a>
         */
        NO_CONTENT(204),
        /**
         * {@code 205 Reset Content}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.6">HTTP/1.1</a>
         */
        RESET_CONTENT(205),
        /**
         * {@code 206 Partial Content}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.7">HTTP/1.1</a>
         */
        PARTIAL_CONTENT(206),
        /**
         * {@code 207 Multi-Status}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
         */
        MULTI_STATUS(207),
        /**
         * {@code 208 Already Reported}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
         */
        ALREADY_REPORTED(208),
        /**
         * {@code 226 IM Used}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
         */
        IM_USED(226),

        // 3xx Redirection

        /**
         * {@code 300 Multiple Choices}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.1">HTTP/1.1</a>
         */
        MULTIPLE_CHOICES(300),
        /**
         * {@code 301 Moved Permanently}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.2">HTTP/1.1</a>
         */
        MOVED_PERMANENTLY(301),
        /**
         * {@code 302 Found}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.3">HTTP/1.1</a>
         */
        FOUND(302),
        /**
         * {@code 302 Moved Temporarily}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0</a>
         * @deprecated In favor of {@link #FOUND} which will be returned from {@code Status.valueOf(302)}
         */
        @Deprecated
        MOVED_TEMPORARILY(302),
        /**
         * {@code 303 See Other}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4">HTTP/1.1</a>
         */
        SEE_OTHER(303),
        /**
         * {@code 304 Not Modified}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.5">HTTP/1.1</a>
         */
        NOT_MODIFIED(304),
        /**
         * {@code 305 Use Proxy}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.6">HTTP/1.1</a>
         */
        USE_PROXY(305),
        /**
         * {@code 307 Temporary Redirect}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.8">HTTP/1.1</a>
         */
        TEMPORARY_REDIRECT(307),
        /**
         * {@code 308 Resume Incomplete}.
         *
         * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
         * resumable POST/PUT HTTP requests in HTTP/1.0</a>
         */
        RESUME_INCOMPLETE(308),

        // --- 4xx Client Error ---

        /**
         * {@code 400 Bad Request}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.1">HTTP/1.1</a>
         */
        BAD_REQUEST(400),
        /**
         * {@code 401 Unauthorized}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.2">HTTP/1.1</a>
         */
        UNAUTHORIZED(401),
        /**
         * {@code 402 Payment Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.3">HTTP/1.1</a>
         */
        PAYMENT_REQUIRED(402),
        /**
         * {@code 403 Forbidden}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.4">HTTP/1.1</a>
         */
        FORBIDDEN(403),
        /**
         * {@code 404 Not Found}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.5">HTTP/1.1</a>
         */
        NOT_FOUND(404),
        /**
         * {@code 405 Method Not Allowed}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.6">HTTP/1.1</a>
         */
        METHOD_NOT_ALLOWED(405),
        /**
         * {@code 406 Not Acceptable}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.7">HTTP/1.1</a>
         */
        NOT_ACCEPTABLE(406),
        /**
         * {@code 407 Proxy Authentication Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.8">HTTP/1.1</a>
         */
        PROXY_AUTHENTICATION_REQUIRED(407),
        /**
         * {@code 408 Request Timeout}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.9">HTTP/1.1</a>
         */
        REQUEST_TIMEOUT(408),
        /**
         * {@code 409 Conflict}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.10">HTTP/1.1</a>
         */
        CONFLICT(409),
        /**
         * {@code 410 Gone}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.11">HTTP/1.1</a>
         */
        GONE(410),
        /**
         * {@code 411 Length Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.12">HTTP/1.1</a>
         */
        LENGTH_REQUIRED(411),
        /**
         * {@code 412 Precondition failed}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.13">HTTP/1.1</a>
         */
        PRECONDITION_FAILED(412),
        /**
         * {@code 413 Request Entity Too Large}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1</a>
         */
        REQUEST_ENTITY_TOO_LARGE(413),
        /**
         * {@code 414 Request-URI Too Long}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1</a>
         */
        REQUEST_URI_TOO_LONG(414),
        /**
         * {@code 415 Unsupported Media Type}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.16">HTTP/1.1</a>
         */
        UNSUPPORTED_MEDIA_TYPE(415),
        /**
         * {@code 416 Requested Range Not Satisfiable}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.17">HTTP/1.1</a>
         */
        REQUESTED_RANGE_NOT_SATISFIABLE(416),
        /**
         * {@code 417 Expectation Failed}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.18">HTTP/1.1</a>
         */
        EXPECTATION_FAILED(417),
        /**
         * {@code 418 I'm a teapot}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
         */
        I_AM_A_TEAPOT(418),
        /**
         * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
         */
        @Deprecated INSUFFICIENT_SPACE_ON_RESOURCE(419),
        /**
         * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
         */
        @Deprecated METHOD_FAILURE(420),
        /**
         * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
         */
        @Deprecated DESTINATION_LOCKED(421),
        /**
         * {@code 422 Unprocessable Entity}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
         */
        UNPROCESSABLE_ENTITY(422),
        /**
         * {@code 423 Locked}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
         */
        LOCKED(423),
        /**
         * {@code 424 Failed Dependency}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
         */
        FAILED_DEPENDENCY(424),
        /**
         * {@code 426 Upgrade Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS Within HTTP/1.1</a>
         */
        UPGRADE_REQUIRED(426),
        /**
         * {@code 428 Precondition Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
         */
        PRECONDITION_REQUIRED(428),
        /**
         * {@code 429 Too Many Requests}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
         */
        TOO_MANY_REQUESTS(429),
        /**
         * {@code 431 Request Header Fields Too Large}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
         */
        REQUEST_HEADER_FIELDS_TOO_LARGE(431),

        // --- 5xx Server Error ---

        /**
         * {@code 500 Internal Server Error}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.1">HTTP/1.1</a>
         */
        INTERNAL_SERVER_ERROR(500),
        /**
         * {@code 501 Not Implemented}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.2">HTTP/1.1</a>
         */
        NOT_IMPLEMENTED(501),
        /**
         * {@code 502 Bad Gateway}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.3">HTTP/1.1</a>
         */
        BAD_GATEWAY(502),
        /**
         * {@code 503 Service Unavailable}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.4">HTTP/1.1</a>
         */
        SERVICE_UNAVAILABLE(503),
        /**
         * {@code 504 Gateway Timeout}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.5">HTTP/1.1</a>
         */
        GATEWAY_TIMEOUT(504),
        /**
         * {@code 505 HTTP Version Not Supported}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.6">HTTP/1.1</a>
         */
        HTTP_VERSION_NOT_SUPPORTED(505),
        /**
         * {@code 506 Variant Also Negotiates}
         *
         * @see <a href="http://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
         */
        VARIANT_ALSO_NEGOTIATES(506),
        /**
         * {@code 507 Insufficient Storage}
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
         */
        INSUFFICIENT_STORAGE(507),
        /**
         * {@code 508 Loop Detected}
         *
         * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
         */
        LOOP_DETECTED(508),
        /**
         * {@code 509 Bandwidth Limit Exceeded}
         */
        BANDWIDTH_LIMIT_EXCEEDED(509),
        /**
         * {@code 510 Not Extended}
         *
         * @see <a href="http://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
         */
        NOT_EXTENDED(510),
        /**
         * {@code 511 Network Authentication Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
         */
        NETWORK_AUTHENTICATION_REQUIRED(511);

        private final int code;

        private Status(int code) {
            this.code = code;
        }

        /**
         * Return the integer code of this status code.
         */
        public int code() {
            return this.code;
        }

        /**
         * Returns true if the status is a client error or server error
         *
         * @see #isServerError()
         * @see #isClientError()
         */
        public boolean isError() {
            return isClientError() || isServerError();
        }

        /**
         * Returns true if the status is server error (5xx)
         */
        public boolean isServerError() {
            return code / 100 == 5;
        }

        /**
         * Returns true if the status is client error (4xx)
         */
        public boolean isClientError() {
            return code / 100 == 4;
        }

        /**
         * Returns true if the status is success series (2xx)
         */
        public boolean isSuccess() {
            return code / 100 == 2;
        }

        /**
         * Returns true if the status is redirect series (3xx)
         */
        public boolean isRedirect() {
            return code / 100 == 3;
        }

        /**
         * Returns true if the status is informational series (1xx)
         */
        public boolean isInformational() {
            return code / 100 == 1;
        }

        /**
         * Return a string representation of this status code.
         */
        @Override
        public String toString() {
            return Integer.toString(code);
        }


        /**
         * Return the enum constant of this type with the specified numeric code.
         *
         * @param statusCode the numeric code of the enum to be returned
         * @return the enum constant with the specified numeric code
         * @throws IllegalArgumentException if this enum has no constant for the specified numeric code
         */
        public static Status valueOf(int statusCode) {
            for (Status status : values()) {
                if (status.code == statusCode) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        }
    } // eof Status

    public static final class Header {
        public static final class Names {
            /**
             * {@code "Accept"}
             */
            public static final String ACCEPT = "accept";
            /**
             * {@code "Accept-Charset"}
             */
            public static final String ACCEPT_CHARSET = "accept-charset";
            /**
             * {@code "Accept-Encoding"}
             */
            public static final String ACCEPT_ENCODING = "accept-encoding";
            /**
             * {@code "Accept-Language"}
             */
            public static final String ACCEPT_LANGUAGE = "accept-language";
            /**
             * {@code "Accept-Ranges"}
             */
            public static final String ACCEPT_RANGES = "accept-ranges";
            /**
             * {@code "Accept-Patch"}
             */
            public static final String ACCEPT_PATCH = "accept-patch";
            /**
             * {@code "Age"}
             */
            public static final String AGE = "age";
            /**
             * {@code "Allow"}
             */
            public static final String ALLOW = "allow";
            /**
             * {@code "Authorization"}
             */
            public static final String AUTHORIZATION = "authorization";
            /**
             * {@code "Cache-Control"}
             */
            public static final String CACHE_CONTROL = "cache-control";
            /**
             * {@code "Connection"}
             */
            public static final String CONNECTION = "connection";
            /**
             * {@code "Content-Base"}
             */
            public static final String CONTENT_BASE = "content-base";
            /**
             * {@code "Content-Encoding"}
             */
            public static final String CONTENT_ENCODING = "content-encoding";
            /**
             * {@code "Content-Language"}
             */
            public static final String CONTENT_LANGUAGE = "content-language";
            /**
             * {@code "Content-Length"}
             */
            public static final String CONTENT_LENGTH = "content-length";
            /**
             * {@code "Content-Location"}
             */
            public static final String CONTENT_LOCATION = "content-location";
            /**
             * {@code "Content-Transfer-Encoding"}
             */
            public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
            /**
             * {@code "Content-MD5"}
             */
            public static final String CONTENT_MD5 = "content-md5";
            /**
             * {@code "Content-Range"}
             */
            public static final String CONTENT_RANGE = "content-range";
            /**
             * {@code "Content-Type"}
             */
            public static final String CONTENT_TYPE = "content-type";
            /**
             * {@code "Cookie"}
             */
            public static final String COOKIE = "cookie";
            /**
             * {@code "Date"}
             */
            public static final String DATE = "date";
            /**
             * {@code "ETag"}
             */
            public static final String ETAG = "etag";
            /**
             * {@code "Expect"}
             */
            public static final String EXPECT = "expect";
            /**
             * {@code "Expires"}
             */
            public static final String EXPIRES = "expires";
            /**
             * {@code "From"}
             */
            public static final String FROM = "from";
            /**
             * {@code "Host"}
             */
            public static final String HOST = "host";
            /**
             * {@code "If-Match"}
             */
            public static final String IF_MATCH = "if-match";
            /**
             * {@code "If-Modified-Since"}
             */
            public static final String IF_MODIFIED_SINCE = "if-modified-since";
            /**
             * {@code "If-None-Match"}
             */
            public static final String IF_NONE_MATCH = "if-none-natch";
            /**
             * {@code "If-Range"}
             */
            public static final String IF_RANGE = "if-range";
            /**
             * {@code "If-Unmodified-Since"}
             */
            public static final String IF_UNMODIFIED_SINCE = "if-unmodified-since";
            /**
             * {@code "Last-Modified"}
             */
            public static final String LAST_MODIFIED = "last-modified";
            /**
             * {@code "Location"}
             */
            public static final String LOCATION = "location";
            /**
             * {@code "Max-Forwards"}
             */
            public static final String MAX_FORWARDS = "max-forwards";
            /**
             * {@code "Origin"}
             */
            public static final String ORIGIN = "origin";
            /**
             * {@code "Pragma"}
             */
            public static final String PRAGMA = "pragma";
            /**
             * {@code "Proxy-Authenticate"}
             */
            public static final String PROXY_AUTHENTICATE = "proxy-authenticate";
            /**
             * {@code "Proxy-Authorization"}
             */
            public static final String PROXY_AUTHORIZATION = "proxy-authorization";
            /**
             * {@code "Range"}
             */
            public static final String RANGE = "range";
            /**
             * {@code "Referer"}
             */
            public static final String REFERER = "referer";
            /**
             * {@code "Retry-After"}
             */
            public static final String RETRY_AFTER = "retry-after";
            /**
             * {@code "sec-websocket-Key1"}
             */
            public static final String SEC_WEBSOCKET_KEY1 = "sec-websocket-key1";
            /**
             * {@code "sec-websocket-Key2"}
             */
            public static final String SEC_WEBSOCKET_KEY2 = "sec-websocket-key2";
            /**
             * {@code "sec-websocket-Location"}
             */
            public static final String SEC_WEBSOCKET_LOCATION = "sec-websocket-location";
            /**
             * {@code "sec-websocket-Origin"}
             */
            public static final String SEC_WEBSOCKET_ORIGIN = "sec-websocket-origin";
            /**
             * {@code "sec-websocket-Protocol"}
             */
            public static final String SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
            /**
             * {@code "sec-websocket-Version"}
             */
            public static final String SEC_WEBSOCKET_VERSION = "sec-websocket-version";
            /**
             * {@code "sec-websocket-Key"}
             */
            public static final String SEC_WEBSOCKET_KEY = "sec-websocket-key";
            /**
             * {@code "sec-websocket-Accept"}
             */
            public static final String SEC_WEBSOCKET_ACCEPT = "sec-websocket-accept";
            /**
             * {@code "Server"}
             */
            public static final String SERVER = "server";
            /**
             * {@code "Set-Cookie"}
             */
            public static final String SET_COOKIE = "set-cookie";
            /**
             * {@code "Set-Cookie2"}
             */
            public static final String SET_COOKIE2 = "set-cookie2";
            /**
             * {@code "TE"}
             */
            public static final String TE = "te";
            /**
             * {@code "Trailer"}
             */
            public static final String TRAILER = "trailer";
            /**
             * {@code "Transfer-Encoding"}
             */
            public static final String TRANSFER_ENCODING = "transfer-encoding";
            /**
             * {@code "Upgrade"}
             */
            public static final String UPGRADE = "upgrade";
            /**
             * {@code "User-Agent"}
             */
            public static final String USER_AGENT = "user-agent";
            /**
             * {@code "Vary"}
             */
            public static final String VARY = "vary";
            /**
             * {@code "Via"}
             */
            public static final String VIA = "via";
            /**
             * {@code "Warning"}
             */
            public static final String WARNING = "warning";
            /**
             * {@code "WebSocket-Location"}
             */
            public static final String WEBSOCKET_LOCATION = "websocket-location";
            /**
             * {@code "WebSocket-Origin"}
             */
            public static final String WEBSOCKET_ORIGIN = "webwocket-origin";
            /**
             * {@code "WebSocket-Protocol"}
             */
            public static final String WEBSOCKET_PROTOCOL = "websocket-protocol";
            /**
             * {@code "WWW-Authenticate"}
             */
            public static final String WWW_AUTHENTICATE = "www-authenticate";

            /**
             * {@code X_Requested_With}
             */
            public static final String X_REQUESTED_WITH = "x-requested-with";

            private Names() {
                super();
            }
        }

        private String name;
        private C.List<String> values;

        public Header(String name, String value) {
            E.NPE(name);
            this.name = name;
            this.values = C.list(value);
        }

        public Header(String name, String... values) {
            E.NPE(name);
            this.name = name;
            this.values = C.listOf(values);
        }

        public Header(String name, Iterable<String> values) {
            E.NPE(name);
            this.name = name;
            this.values = C.list(values);
        }

        public String name() {
            return name;
        }

        public String value() {
            return values.get(0);
        }

        public C.List<String> values() {
            return values;
        }

        @Override
        public String toString() {
            return values.toString();
        }
    } // eof Header


    /**
     * Specify the format of the requested content type
     */
    public static enum Format {
        /**
         * The "text/html" content format
         */
        html {
            @Override
            public String toContentType() {
                return "text/html";
            }
        },
        /**
         * The "text/xml" content format
         */
        xml {
            @Override
            public String toContentType() {
                return "text/xml";
            }
        },
        /**
         * The "application/json" content format
         */
        json {
            @Override
            public String toContentType() {
                return "application/json";
            }

            /**
             * Returns {@code {"error": "[error-message]"}} string
             * @param message the error message
             * @return the error message in JSON formatted string
             */
            @Override
            public String errorMessage(String message) {
                return S.fmt("{\"error\": \"%s\"}", message);
            }
        },

        /**
         * The "application/vnd.ms-excel" content format
         */
        xls {
            @Override
            public String toContentType() {
                return "application/vnd.ms-excel";
            }
        },
        /**
         * The "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" content format
         */
        xlsx {
            @Override
            public String toContentType() {
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }
        },
        /**
         * The "application/vnd.ms-word" content format
         */
        doc {
            @Override
            public String toContentType() {
                return "application/vnd.ms-word";
            }
        },
        /**
         * The "application/vnd.openxmlformats-officedocument.wordprocessingml.document" content format
         */
        docx {
            @Override
            public String toContentType() {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
        },
        /**
         * The "text/csv" content format
         */
        csv {
            @Override
            public String toContentType() {
                return "text/csv";
            }
        },
        /**
         * The "text/plain" content format
         */
        txt {
            @Override
            public String toContentType() {
                return "text/plain";
            }
        };

        /**
         * Returns the content type string
         *
         * @return the content type string of this format
         */
        public abstract String toContentType();

        /**
         * Returns the error message
         *
         * @param message
         * @return the message directly
         */
        public String errorMessage(String message) {
            return message;
        }

        /**
         * Resolve {@code Format} instance out of an http "Accept" header.
         *
         * @param accept the value of http "Accept" header
         * @return an {@code Format} instance
         */
        public static Format resolve(String accept) {
            return resolve_(Format.html, accept);
        }

        public static Format resolve(Format def, String accept) {
            E.NPE(def);
            return resolve_(def, accept);
        }

        private static Format resolve_(Format def, String accept) {
            Format fmt = def;
            if (S.empty(accept)) {
                fmt = html;
            } else if (accept.contains("application/xhtml") || accept.contains("text/html") || accept.startsWith("*/*")) {
                fmt = html;
            } else if (accept.contains("application/xml") || accept.contains("text/xml")) {
                fmt = xml;
            } else if (accept.contains("application/json") || accept.contains("text/javascript")) {
                fmt = json;
            } else if (accept.contains("text/plain")) {
                fmt = txt;
            } else if (accept.contains("csv") || accept.contains("comma-separated-values")) {
                fmt = csv;
            } else if (accept.contains("ms-excel")) {
                fmt = xls;
            } else if (accept.contains("spreadsheetml")) {
                fmt = xlsx;
            } else if (accept.contains("msword")) {
                fmt = doc;
            } else if (accept.contains("wordprocessingml")) {
                fmt = docx;
            }

            return fmt;
        }

        public static Format resolve(Iterable<String> accepts) {
            return resolve(Format.html, accepts);
        }

        public static Format resolve(Format def, Iterable<String> accepts) {
            Format retval;
            for (String s : accepts) {
                retval = resolve_(null, s);
                if (null != retval) {
                    return retval;
                }
            }
            return _.ifNullThen(def, Format.html);
        }

        public static Format resolve(String... accepts) {
            return resolve(Format.html, accepts);
        }

        public static Format resolve(Format def, String... accepts) {
            Format retval;
            for (String s : accepts) {
                retval = resolve_(null, s);
                if (null != retval) {
                    return retval;
                }
            }
            return _.ifNullThen(def, Format.html);
        }
    } // eof Format

    /**
     * Defines the HTTP cookie trait
     *
     * @param <T> the type of the implementation class
     */
    public static interface Cookie<T extends Cookie> {

        /**
         * Returns the class of the Cookie implementation.
         * Only used for Java generic typing. Do not use
         * in your application
         */
        Class<T> _impl();

        /**
         * Returns the name of the cookie. Cookie name
         * cannot be changed after created
         */
        String name();

        /**
         * Returns the value of the cookie
         */
        String value();

        /**
         * Set a value to a cookie and the return {@code this} cookie
         *
         * @param value the value to be set to the cookie
         * @return this cookie
         */
        T value(String value);

        /**
         * Returns the domain of the cookie
         */
        String domain();

        /**
         * Set the domain of the cookie
         *
         * @param domain the  domain string
         * @return this cookie
         */
        T domain(String domain);

        /**
         * Returns the path on the server
         * to which the browser returns this cookie. The
         * cookie is visible to all subpaths on the server.
         *
         * @see #path(String)
         */
        String path();

        /**
         * Specifies a path for the cookie
         * to which the client should return the cookie.
         * <p/>
         * <p>The cookie is visible to all the pages in the directory
         * you specify, and all the pages in that directory's subdirectories.
         * <p/>
         * <p>Consult RFC 2109 (available on the Internet) for more
         * information on setting path names for cookies.
         *
         * @param uri a <code>String</code> specifying a path
         * @return this cookie after path is set
         * @see #path
         */
        T path(String uri);

        /**
         * Returns the maximum age of cookie specified in seconds. If
         * maxAge is set to {@code -1} then the cookie will persist until
         * browser shutdown
         */
        int maxAge();

        /**
         * Set the max age of the cookie in seconds.
         *
         * @see #maxAge()
         */
        T maxAge(int maxAge);

        /**
         * Returns <code>true</code> if the browser is sending cookies
         * only over a secure protocol, or <code>false</code> if the
         * browser can send cookies using any protocol.
         *
         * @see #secure(boolean)
         */
        boolean secure();

        /**
         * Indicates to the browser whether the cookie should only be sent
         * using a secure protocol, such as HTTPS or SSL.
         * <p/>
         * <p>The default value is <code>false</code>.
         *
         * @param secure the cookie secure requirement
         * @return this cookie instance
         */
        T secure(boolean secure);

    } // eof Cookie

    /**
     * Defines a data structure to encapsulate a stateless session which
     * accept only {@code String} type value, and will be persisted at
     * client side as a cookie. This means the entire size of the
     * information stored in session including names and values shall
     * not exceed 4096 bytes.
     * <p/>
     * <p>To store typed value or big value, use the cache methods
     * of the session class. However it is subject to the implementation
     * to decide whether cache methods are provided and how it is
     * implemented</p>
     */
    public static final class Session {

        private static final String ID_KEY = "___ID";
        private static final String TS_KEY = "___TS";

        private C.Map<String, String> data = C.newMap();
        private String id;
        private boolean dirty = false;

        public Session() {
        }

        @Override
        public String toString() {
            return data.toString();
        }

        private void change() {
            dirty = true;
        }

        /**
         * Returns the session identifier
         */
        public String id() {
            if (null == id) {
                id = UUID.randomUUID().toString();
                data.put(ID_KEY, id());
            }
            return id;
        }

        // ------- regular session attribute operations ---

        /**
         * Set a session attribute. If the attribute already exists
         * then the value is updated; otherwise an attribute is added
         * to the session
         *
         * @param key   the attribute key
         * @param value the attribute value in string
         * @return this session instance
         */
        public Session put(String key, String value) {
            change();
            data.put(key, value);
            return this;
        }

        /**
         * Set a session attribute with {@code Object} type value.
         * However the session stores the value's
         * {@link Object#toString() string representation}
         * only
         *
         * @param key   the attribute key
         * @param value the attribute value in Object
         * @return this session instance
         */
        public Session put(String key, Object value) {
            return put(key, null == value ? (String) null : S.string(value));
        }

        /**
         * Returns an attribute value by name
         *
         * @param key the attribute key
         * @return the value of the attribute or {@code null} if
         * the attribute cannot be found by name
         */
        public String get(String key) {
            return data.get(key);
        }

        /**
         * Removes an attribute from session specified by key
         *
         * @param key the key of the attribute to be removed
         * @return the session instance
         */
        public Session remove(String key) {
            change();
            data.remove(key);
            return this;
        }

        /**
         * Removes an array of attributes from session specified by
         * key array
         *
         * @param key  the first attribute key
         * @param keys the rest attribute keys
         * @return this session object
         */
        public Session remove(String key, String... keys) {
            change();
            data.remove(key);
            for (String k : keys) {
                data.remove(k);
            }
            return this;
        }

        /**
         * Remove all attributes stored with the session
         *
         * @return this session instance
         */
        public Session clear() {
            if (data.isEmpty()) return this;
            change();
            data.clear();
            return this;
        }

        /**
         * Returns {@code true} if the session is empty. e.g.
         * does not contain anything else than the timestamp
         */
        public boolean empty() {
            for (String key : data.keySet()) {
                if (!TS_KEY.equals(key)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Check if the session has an attribute specified by key
         *
         * @param key the attribute key
         * @return {@code true} if the session contains the attribute
         * with key specified
         */
        public boolean contains(String key) {
            return data.containsKey(key);
        }

        // ------- eof regular session attribute operations ---

        // ------- cache operations ------

        /*
         * Attach session id to a cache key
         */
        private String k(String key) {
            return S.builder(id()).append(key).toString();
        }

        private static volatile CacheService cs;

        private static CacheService cs() {
            if (null != cs) return cs;
            synchronized (H.class) {
                if (null == cs) {
                    cs = Config.cacheService();
                }
                return cs;
            }
        }

        /**
         * Store an object into cache using key specified. The key will be
         * appended with session id, so that it distinct between caching
         * using the same key but in different user sessions.
         * <p/>
         * <p>The object is cached for {@link org.osgl.cache.CacheService#setDefaultTTL(int) default} ttl</p>
         *
         * @param key the key to cache the object
         * @param obj the object to be cached
         * @return this session instance
         */
        public Session cache(String key, Object obj) {
            cs().put(k(key), obj);
            return this;
        }

        /**
         * Store an object into cache with expiration specified
         *
         * @param key        the key to cache the object
         * @param obj        the object to be cached
         * @param expiration specify the cache expiration in seconds
         * @return this session instance
         * @see #cache(String, Object)
         */
        public Session cache(String key, Object obj, int expiration) {
            cs().put(k(key), obj, expiration);
            return this;
        }


        /**
         * Store an object into cache for 1 hour
         *
         * @param key the key to cache the object
         * @param obj the object to be cached
         * @return the session instance
         */
        public Session cacheFor1Hr(String key, Object obj) {
            return cache(key, obj, 60 * 60);
        }


        /**
         * Store an object into cache for 30 minutes
         *
         * @param key the key to cache the object
         * @param obj the object to be cached
         * @return the session instance
         */
        public Session cacheFor30Min(String key, Object obj) {
            return cache(key, obj, 30 * 60);
        }


        /**
         * Store an object into cache for 10 minutes
         *
         * @param key the key to cache the object
         * @param obj the object to be cached
         * @return the session instance
         */
        public Session cacheFor10Min(String key, Object obj) {
            return cache(key, obj, 10 * 60);
        }

        /**
         * Store an object into cache for 1 minutes
         *
         * @param key the key to cache the object
         * @param obj the object to be cached
         * @return the session instance
         */
        public Session cacheFor1Min(String key, Object obj) {
            return cache(key, obj, 60);
        }

        /**
         * Evict an object from cache
         * @param key the key to cache the object
         * @return this session instance
         */
        public Session evict(String key) {
            cs().evict(k(key));
            return this;
        }

        /**
         * Retrieve an object from cache by key. The key
         * will be attached with session id
         *
         * @param key the key to get the cached object
         * @param <T> the object type
         * @return the object in the cache, or {@code null}
         *         if it cannot find the object by key
         *         specified
         * @see #cache(String, Object)
         */
        public <T> T cached(String key) {
            return cs.get(k(key));
        }

        /**
         * Retrieve an object from cache by key. The key
         * will be attached with session id
         *
         * @param key the key to get the cached object
         * @param clz the class to specify the return type
         * @param <T> the object type
         *
         * @return the object in the cache, or {@code null}
         *         if it cannot find the object by key
         *         specified
         * @see #cache(String, Object)
         */
        public <T> T cached(String key, Class<T> clz) {
            return cs.get(k(key));
        }
        // ------- eof cache operations ------

        /**
         * Return a session instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         * @return the current session instance
         */
        public static Session current() {
            return Current.session();
        }

        /**
         * Set a session instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         * @param session the session to be set to current execution context
         */
        public static void current(Session session) {
            Current.session(session);
        }


    } // eof Session

    /**
     * A Flash represent a storage scope that attributes inside is valid only
     * for one session interaction. This feature of flash makes it very good
     * for server to pass one time information to client, e.g. form submission
     * error message etc.
     *
     * <p>Like {@link org.osgl.http.H.Session}, you can store only String type
     * information to flash, and the total number of information stored
     * including keys and values shall not exceed 4096 bytes as flash is
     * persisted as cookie in browser</p>
     */
    public static final class Flash {

        private Map<String, String> data = new HashMap<String, String>();
        private Map<String, String> out = new HashMap<String, String>();
        private static Pattern flashParser = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");


        /**
         * Add an attribute to the flash scope. The data is
         * added to both data buffer and the out buffer
         *
         * @param key the key to index the attribute
         * @param value the value of the attribute
         * @return the flash instance
         */
        public Flash put(String key, String value) {
            E.illegalArgumentIf(key.contains(":"), "Character ':' is invalid in a flash key.");
            data.put(key, value);
            out.put(key, value);
            return this;
        }

        /**
         * Add an attribute to the flash scope. The value is in Object
         * type, however it will be convert to its {@link Object#toString() string
         * representation} before put into the flash
         * @param key the key to index the attribute
         * @param value the value to be put into the flash
         * @return this flash instance
         */
        public Flash put(String key, Object value) {
            return put(key, null == value ? (String)null : value.toString());
        }

        /**
         * Add an attribute to the flash's current scope. Meaning when next time
         * the user request to the server, the attribute will not be there anymore.
         *
         * @param key the attribute key
         * @param value the attribute value
         * @return the flash instance
         */
        public Flash now(String key, String value) {
            if (key.contains(":")) {
                throw new IllegalArgumentException("Character ':' is invalid in a flash key.");
            }
            data.put(key, value);
            return this;
        }

        /**
         * Check if the data buffer contains a flash data
         * identified by the key specified
         * @param key the key to identify the flash data
         * @return {@code true} if the flash data contains
         *         the data specified by key
         */
        public boolean contains(String key) {
            return data.containsKey(key);
        }

        /**
         * Get a data from the flash by key
         *
         * @param key identifies the data in the scope
         * @return the data in the scope identified by key specified
         */
        public String get(String key) {
            return data.get(key);
        }


        /**
         * Remove a data from the flash scope specified by key.
         * However if the data is already {@link #keep(String) kept}
         * in the out buffer is not removed. to remove the data
         * in the out buffer, use {@link #discard(String)} instead
         *
         * @param key identifies the data in the flash
         * @return the flash instance
         * @see #discard(String)
         */
        public Flash remove(String key) {
            data.remove(key);
            return this;
        }

        /**
         * Clear the flash data. However the data already
         * been {@link #keep() kept} in the out buffer is
         * not cleared. To clean the out buffer, use
         * {@link #discard()}
         *
         * @return the flash instance
         * @see #discard()
         */
        public Flash clear() {
            data.clear();
            return this;
        }

        /**
         * Add an "error" message to the flash scope
         *
         * @param message the error message
         * @return the flash instance
         * @see #put(String, Object)
         */
        public Flash error(String message) {
            return put("error", message);
        }

        /**
         * Add an "error" message to the flash scope, with
         * optional format arguments
         *
         * @param message the message template
         * @param args the format arguments
         * @return this flash instance
         */
        public Flash error(String message, Object... args) {
            return put("error", S.fmt(message, args));
        }

        /**
         * Get the "error" message that has been added to
         * the flash scope.
         * @return the "error" message or {@code null} if
         *         no error message has been added to the flash
         */
        public String error() {
            return get("error");
        }

        /**
         * Add a "success" message to the flash scope
         *
         * @param message the error message
         * @return the flash instance
         * @see #put(String, Object)
         */
        public Flash success(String message) {
            return put("success", message);
        }

        /**
         * Add a "success" message to the flash scope, with
         * optional format arguments
         *
         * @param message the message template
         * @param args the format arguments
         * @return this flash instance
         */
        public Flash success(String message, Object... args) {
            return put("success", S.fmt(message, args));
        }

        /**
         * Get the "success" message that has been added to
         * the flash scope.
         * @return the "success" message or {@code null} if
         *         no success message has been added to the flash
         */
        public String success() {
            return get("success");
        }

        /**
         * Discard a data from the output buffer of the flash but
         * the data buffer is remain untouched. Meaning
         * the app can still get the data {@link #put(String, Object)}
         * into the flash scope, however they will NOT
         * be write to the client cookie, thus the next
         * time client request the server, the app will
         * not be able to get the info anymore
         *
         * @param key the key to the data to be discarded
         * @return the flash instance
         */
        public Flash discard(String key) {
            out.remove(key);
            return this;
        }

        /**
         * Discard the whole output buffer of the flash but
         * the data buffer is remain untouched. Meaning
         * the app can still get the data {@link #put(String, Object)}
         * into the flash scope, however they will NOT
         * be write to the client cookie, thus the next
         * time client request the server, the app will
         * not be able to get those info anymore
         *
         * @return the flash instance
         */
        public Flash discard() {
            out.clear();
            return this;
        }

        /**
         * Keep a data that has been {@link #put(String, Object) put}
         * into the flash for one time. The data that has been kept
         * will be persistent to client cookie for one time, thus
         * the next time when user request the server, the app
         * can still get the data, but only for one time unless
         * the app call {@code keep} method again
         *
         * @param key the key to identify the data to be kept
         * @see #keep()
         */
        public Flash keep(String key) {
            if (data.containsKey(key)) {
                out.put(key, data.get(key));
            }
            return this;
        }

        /**
         * Keep all data that has been {@link #put(String, Object) put}
         * into the flash for one time. The data that has been kept
         * will be persistent to client cookie for one time, thus
         * the next time when user request the server, the app
         * can still get the data, but only for one time unless
         * the app call {@code keep} method again
         *
         * @return the flash instance
         */
        public Flash keep() {
            out.putAll(data);
            return this;
        }

        @Override
        public String toString() {
            return data.toString();
        }

        /**
         * Return a flash instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         * @return the current flash instance
         */
        public static Flash current() {
            return Current.flash();
        }

        /**
         * Set a flash instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         * @param flash the flash to be set to current execution context
         */
        public static void current(Flash flash) {
            Current.flash(flash);
        }

    } // eof Flash

    /**
     * Defines the HTTP request trait
     *
     * @param <T> the type of the implementation class
     */
    public static abstract class Request<T extends Request> {

        /**
         * Returns the class of the implementation. Not to be used
         * by application
         */
        protected abstract Class<T> _impl();

        private static SimpleDateFormat dateFormat; static {
            dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        private Format fmt;

        /**
         * Returns the HTTP method of the request
         */
        public abstract Method method();

        /**
         * Returns the header content by name. If there are
         * multiple headers with the same name, then the first
         * one is returned. If there is no header has the name
         * then {@code null} is returned
         *
         * <p>Note header name is case insensitive</p>
         *
         * @param name the name of the header
         * @return the header content
         */
        public abstract String header(String name);

        /**
         * Returns all header content by name. This method returns
         * content of all header with the same name specified in
         * an {@link java.lang.Iterable} of {@code String}. If there
         * is no header has the name specified, then an empty iterable
         * is returned.
         *
         * <p>Note header name is case insensitive</p>
         *
         * @param name the name of the header
         * @return all values of the header
         */
        public abstract Iterable<String> headers(String name);

        /**
         * Return the request {@link org.osgl.http.H.Format format}
         *
         * @return the request format
         */
        public Format format() {
            if (null == fmt) {
                resolveFormat();
            }
            return fmt;
        }

        /**
         * Check if the request is an ajax call
         * @return {@code true} if it is an ajax call
         */
        public boolean isAjax() {
            return S.eq(header(X_REQUESTED_WITH), "XMLHttpRequest");
        }

        /**
         * Returns the path of the request. This does not include the
         * context path. The path is a composite of
         * {@link javax.servlet.http.HttpServletRequest#getServletPath()}
         * and {@link javax.servlet.http.HttpServletRequest#getPathInfo()}
         */
        public abstract String path();

        /**
         * Returns the context path of the request.
         */
        public abstract String contextPath();

        /**
         * Returns the full URI path. It's composed of
         * {@link #contextPath()} and {@link #path()}
         */
        public String fullPath() {
            String cp = contextPath();
            String p = path();
            if (S.empty(cp)) return p;
            if (S.empty(p)) return p;
            boolean hasEndSlash = cp.endsWith("/");
            boolean hasLeadingSlash = p.startsWith("/");
            StringBuilder sb = S.builder(cp);
            if (hasEndSlash) {
                if (hasLeadingSlash) {
                    sb.append(p.substring(1));
                } else {
                    sb.append(p);
                }
            } else {
                if (hasLeadingSlash) {
                    sb.append(p);
                } else {
                    sb.append("/").append(p);
                }
            }
            return sb.toString();
        }

        /**
         * Returns query string or an empty String if the request
         * doesn't contains a query string
         */
        public abstract String query();

        /**
         * Check if the request was made on a secure channel
         *
         * @return {@code true} if this is a secure request
         */
        public abstract boolean secure();

        /**
         * Returns the scheme of the request, specifically one of the
         * "http" and "https"
         *
         * @return the scheme of the request
         */
        public String scheme() {
            return secure() ? "https" : "http";
        }

        private String domain;

        private void parseHost() {
            String host = header("host");
            port = secure() ? 80 : 443;
            if (null == host) {
                domain = "";
            } else {
                if (host.contains(":")) {
                    domain = S.before(host, ":");
                    try {
                        port = Integer.parseInt(S.after(host, ":"));
                    } catch (NumberFormatException e) {
                        logger.error(e, "Error parsing port number: %s", S.after(host, ":"));
                    }
                } else {
                    domain = host;
                }
            }
        }

        /**
         * Returns the domain of the request
         */
        public String domain() {
            if (null == domain) parseHost();
            return domain;
        }

        private int port = -1;
        /**
         * Returns the port
         */
        public int port() {
            if (-1 == port) parseHost();
            return port;
        }

        /**
         * resolve the request format
         *
         * @return this request instance
         */
        public Request resolveFormat() {
            String accept = header(ACCEPT);
            fmt = Format.resolve(accept);
            return this;
        }

        /**
         * Check if the requested resource is modified with etag and
         * last timestamp (usually the timestamp of a static file e.g.)
         *
         * @param etag the etag to compare with "If_None_Match"
         *             header in browser
         * @param since the last timestamp to compare with
         *            "If_Modified_Since" header in browser
         * @return {@code true} if the resource has changed
         *            or {@code false} otherwise
         */
        public boolean isModified(String etag, long since) {
            String browserEtag = header(IF_NONE_MATCH);
            if (null == browserEtag) return true;
            if (!S.eq(browserEtag, etag)) {
                return true;
            }

            String s = header(IF_MODIFIED_SINCE);
            if (null == s) return true;
            try {
                Date browserDate = dateFormat.parse(s);
                if (browserDate.getTime() >= since) {
                    return false;
                }
            } catch (ParseException ex) {
                logger.error(ex, "Can't parse date: %s", s);
            }
            return true;
        }

        private void parseContentTypeAndEncoding() {
            String type = header(CONTENT_TYPE);
            if (null == type) {
                contentType = "text/html";
                encoding = "utf-8";
            } else {
                String[] contentTypeParts = type.split(";");
                String _contentType = contentTypeParts[0].trim().toLowerCase();
                String _encoding = null;
                // check for encoding-info
                if( contentTypeParts.length >= 2 ) {
                    String[] encodingInfoParts = contentTypeParts[1].split(("="));
                    if( encodingInfoParts.length == 2 && encodingInfoParts[0].trim().equalsIgnoreCase("charset")) {
                        // encoding-info was found in request
                        _encoding = encodingInfoParts[1].trim();

                        if (S.notEmpty(_encoding) &&
                                ((_encoding.startsWith("\"") && _encoding.endsWith("\""))
                                        || (_encoding.startsWith("'") && _encoding.endsWith("'")))
                                ) {
                            _encoding = _encoding.substring(1, _encoding.length() - 1).trim();
                        }
                    }
                }
                contentType = _contentType;
                encoding = _encoding;
            }
        }

        private String contentType;

        public String contentType() {
            if (null == contentType) {
                parseContentTypeAndEncoding();
            }
            return contentType;
        }

        private String encoding;

        public String encoding() {
            if (null == encoding) {
                parseContentTypeAndEncoding();
            }
            return encoding;
        }

        private C.List<Locale> locales;

        private void parseLocales() {
            String s = header(ACCEPT_LANGUAGE);
            if (S.empty(s)) {
                locales = C.list(Config.defaultLocale());
                return;
            }

            // preprocess to remove all blanks
            s = S.str(s).remove(new _.F1<Character, Boolean>() {
                @Override
                public Boolean apply(Character character) {
                    char c = character;
                    return c == ' ' || c == '\t';
                }
            }).toString();

            ListBuilder<Locale> lb = ListBuilder.create();
            // parse things like "da,en-gb;q=0.8,en;q=0.7"
            String[] sa = s.split(",");
            for (String s0 : sa) {
                // just ignore q=xx
                s0 = S.beforeFirst(s0, ";");
                String[] sa1 = s0.split("-");
                String lang = sa1[0];
                String country = "";
                String variant = "";
                if (sa1.length > 1) {
                    country = sa[1];
                }
                if (sa1.length > 2) {
                    variant = sa[2];
                }
                lb.add(new Locale(lang, country, variant));
            }

            locales = lb.toList();
        }

        public Locale locale() {
            if (null == locales) parseLocales();
            return locales.get(0);
        }

        public C.List<Locale> locales() {
            if (null == locales) parseLocales();
            return locales;
        }

        private int len = -2;

        /**
         * Returns the content length of the request
         */
        public int contentLength() {
            if (len > -2) return len;
            String s = header(CONTENT_LENGTH);
            if (S.empty(s)) {
                len = -1;
            } else {
                try {
                    len = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    len = -1;
                    logger.error("Error parsing content-length: %s", s);
                }
            }
            return len;
        }

        /**
         * Returns body of the request as binary data using {@link java.io.InputStream}
         *
         * @throws IllegalStateException if {@link #reader()} has already
         *              been called on this request instance
         */
        public abstract InputStream inputStream() throws IllegalStateException;

        /**
         * Returns body of the request as binary data using {@link java.io.Reader}
         *
         * @throws IllegalStateException if {@link #inputStream()} has already
         *              been called on this request instance
         */
        public abstract Reader reader() throws IllegalStateException;

        /**
         * Return a request parameter value by name. If there is no parameter
         * found with the name specified, then {@code null} is returned
         *
         * @param name the parameter name
         * @return the parameter value of {@code null} if not found
         */
        public abstract String param(String name);

        /**
         * Return all parameter names
         * @return an {@link java.lang.Iterable} of parameter names
         */
        public abstract Iterable<String> paramNames();


        private void parseAuthorization() {
            if (null != user) return;

            user = "";
            password = "";
            String s = header(AUTHORIZATION);
            if (s.startsWith("Basic")) {
                String data = s.substring(6);
                String[] decodedData = new String(Codec.decodeBASE64(data)).split(":");
                user = decodedData.length > 0 ? decodedData[0] : null;
                password = decodedData.length > 1 ? decodedData[1] : null;
            }
        }

        private String user;

        /**
         * The Http Basic user
         */
        public String user() {
            if (null == user) parseAuthorization();
            return user;
        }

        private String password;

        /**
         * the Http Basic password
         */
        public String password() {
            if (null == password) parseAuthorization();
            return password;
        }

        /**
         * Return a request instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         * @return the current request instance
         */
        @SuppressWarnings("unchecked")
        public static <T extends Request> T current() {
            return (T)Current.request();
        }

        /**
         * Set a request instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         * @param request the request to be set to current execution context
         */
        public static <T extends Request> void current(T request) {
            Current.request(request);
        }

    } // eof Request

    /**
     * Defines the HTTP response trait
     */
    public static abstract class Response<T extends Response> {

        /**
         * Returns the class of the implementation. Not to be used
         * by application
         */
        protected abstract Class<T> _impl();

        /**
         * Return a request instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         * @return the current request instance
         */
        @SuppressWarnings("unchecked")
        public static <T extends Request> T current() {
            return (T)Current.request();
        }

        /**
         * Set a request instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         * @param request the request to be set to current execution context
         */
        public static <T extends Request> void current(T request) {
            Current.request(request);
        }
    } // eof Response

    H() {
    }

}
