package org.osgl.http;

import org.osgl.$;
import org.osgl.cache.CacheService;
import org.osgl.exception.NotAppliedException;
import org.osgl.exception.UnexpectedIOException;
import org.osgl.http.util.Path;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.storage.ISObject;
import org.osgl.util.*;
import org.osgl.web.util.UserAgent;

import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.osgl.http.H.Header.Names.*;

/**
 * The namespace to access Http features.
 * Alias of {@link org.osgl.http.Http}
 */
public class H {

    protected static final Logger logger = L.get(Http.class);

    public enum Method {
        GET, HEAD, POST, DELETE, PUT, PATCH, TRACE, OPTIONS, CONNECT;

        private static EnumSet<Method> unsafeMethods = EnumSet.of(POST, DELETE, PUT, PATCH);
        private static EnumSet<Method> actionMethods = EnumSet.of(GET, POST, PUT, DELETE);

        /**
         * Returns if this http method is safe, meaning it
         * won't change the state of the server
         *
         * @return true if the method is safe or false otherwise
         * @see #unsafe()
         */
        public boolean safe() {
            return !unsafe();
        }

        /**
         * Returns if this http method is unsafe, meaning
         * it will change the state of the server
         *
         * @return true if the method is unsafe or false otherwise
         * @see #safe()
         */
        public boolean unsafe() {
            return unsafeMethods.contains(this);
        }

        public static Method valueOfIgnoreCase(String method) {
            return valueOf(method.toUpperCase());
        }

        public static EnumSet<Method> actionMethods() {
            return actionMethods.clone();
        }
    } // eof Method

    public static final class Status implements Serializable, Comparable<Status> {

        private static final Map<Integer, Status> predefinedStatus = new LinkedHashMap<Integer, Status>();
        private static final long serialVersionUID = -286619406116817809L;

        private int code;

        private Status(int code) {
            this(code, true);
        }

        private Status(int code, boolean predefined) {
            this.code = code;
            if (predefined) {
                predefinedStatus.put(code, this);
            }
        }

        /**
         * Returns the int value of the status
         * @return status code
         */
        public final int code() {
            return code;
        }

        /**
         * Returns {@code true} if the status is either a {@link #isClientError() client error}
         * or {@link #isServerError() server error}
         * @return if this status error
         */
        public boolean isError() {
            return isClientError() || isServerError();
        }

        /**
         * Returns true if the status is server error (5xx)
         * @return if this status server error
         */
        public boolean isServerError() {
            return code / 100 == 5;
        }

        /**
         * Returns true if the status is client error (4xx)
         * @return if this status client error
         */
        public boolean isClientError() {
            return code / 100 == 4;
        }

        /**
         * Returns true if the status is success series (2xx)
         * @return if this status success series
         */
        public boolean isSuccess() {
            return code / 100 == 2;
        }

        /**
         * Returns true if the status is redirect series (3xx)
         * @return if this status redirect series
         */
        public boolean isRedirect() {
            return code / 100 == 3;
        }

        /**
         * Returns true if the status is informational series (1xx)
         * @return is this status informational series
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

        @Override
        public int hashCode() {
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof Status) {
                Status that = (Status) obj;
                return that.code() == code;
            }

            return false;
        }

        @Override
        public int compareTo(Status o) {
            return code - o.code;
        }

        protected final Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }

        private Object readResolve() {
            Status predefined = predefinedStatus.get(code);
            return null != predefined ? predefined : this;
        }

        /**
         * Alias of {@link #valueOf(int)}
         * @param n the status number
         * @return the status instance
         */
        public static Status of(int n) {
            return valueOf(n);
        }

        public static Status valueOf(int n) {
            E.illegalArgumentIf(n < 100 || n > 599, "invalid http status code: %s", n);
            Status retVal = predefinedStatus.get(n);
            if (null == retVal) {
                retVal = new Status(n, false);
            }
            return retVal;
        }

        public static List<Status> predefined() {
            return C.list(predefinedStatus.values());
        }

        // 1xx Informational

        /**
         * {@code 100 Continue}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.1.1">HTTP/1.1</a>
         */
        public static final Status CONTINUE = new Status(100);
        /**
         * {@code 101 Switching Protocols}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.1.2">HTTP/1.1</a>
         */
        public static final Status SWITCHING_PROTOCOLS = new Status(101);
        /**
         * {@code 102 Processing}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
         */
        public static final Status PROCESSING = new Status(102);
        /**
         * {@code 103 Checkpoint}.
         *
         * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
         * resumable POST/PUT HTTP requests in HTTP/1.0</a>
         */
        public static final Status CHECKPOINT = new Status(103);

        // 2xx Success

        /**
         * {@code 200 OK}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.1">HTTP/1.1</a>
         */
        public static final Status OK = new Status(200);
        /**
         * {@code 201 Created}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.2">HTTP/1.1</a>
         */
        public static final Status CREATED = new Status(201);
        /**
         * {@code 202 Accepted}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.3">HTTP/1.1</a>
         */
        public static final Status ACCEPTED = new Status(202);
        /**
         * {@code 203 Non-Authoritative Information}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.4">HTTP/1.1</a>
         */
        public static final Status NON_AUTHORITATIVE_INFORMATION = new Status(203);
        /**
         * {@code 204 No Content}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.5">HTTP/1.1</a>
         */
        public static final Status NO_CONTENT = new Status(204);
        /**
         * {@code 205 Reset Content}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.6">HTTP/1.1</a>
         */
        public static final Status RESET_CONTENT = new Status(205);
        /**
         * {@code 206 Partial Content}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.7">HTTP/1.1</a>
         */
        public static final Status PARTIAL_CONTENT = new Status(206);
        /**
         * {@code 207 Multi-Status}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
         */
        public static final Status MULTI_STATUS = new Status(207);
        /**
         * {@code 208 Already Reported}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
         */
        public static final Status ALREADY_REPORTED = new Status(208);
        /**
         * {@code 226 IM Used}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
         */
        public static final Status IM_USED = new Status(226);

        /**
         * {@code 278} - Faked http status to handle redirection on ajax case.
         *
         * @see <a href="http://stackoverflow.com/questions/199099/how-to-manage-a-redirect-request-after-a-jquery-ajax-call">this stackoverflow</a>
         */
        public static final Status FOUND_AJAX = new Status(278);

        // 3xx Redirection

        /**
         * {@code 300 Multiple Choices}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.1">HTTP/1.1</a>
         */
        public static final Status MULTIPLE_CHOICES = new Status(300);
        /**
         * {@code 301 Moved Permanently}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.2">HTTP/1.1</a>
         */
        public static final Status MOVED_PERMANENTLY = new Status(301);
        /**
         * {@code 302 Found}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.3">HTTP/1.1</a>
         */
        public static final Status FOUND = new Status(302);
        /**
         * {@code 302 Moved Temporarily}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0</a>
         * @deprecated In favor of {@link #FOUND} which will be returned from {@code Status.valueOf(302)}
         */
        @Deprecated
        public static final Status MOVED_TEMPORARILY = new Status(302);
        /**
         * {@code 303 See Other}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4">HTTP/1.1</a>
         */
        public static final Status SEE_OTHER = new Status(303);
        /**
         * {@code 304 Not Modified}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.5">HTTP/1.1</a>
         */
        public static final Status NOT_MODIFIED = new Status(304);
        /**
         * {@code 305 Use Proxy}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.6">HTTP/1.1</a>
         */
        public static final Status USE_PROXY = new Status(305);
        /**
         * {@code 307 Temporary Redirect}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.8">HTTP/1.1</a>
         */
        public static final Status TEMPORARY_REDIRECT = new Status(307);
        /**
         * {@code 308 Resume Incomplete}.
         *
         * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
         * resumable POST/PUT HTTP requests in HTTP/1.0</a>
         */
        public static final Status RESUME_INCOMPLETE = new Status(308);


        // --- 4xx Client Error ---

        /**
         * {@code 400 Bad Request}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.1">HTTP/1.1</a>
         */
        public static final Status BAD_REQUEST = new Status(400);
        /**
         * {@code 401 Unauthorized}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.2">HTTP/1.1</a>
         */
        public static final Status UNAUTHORIZED = new Status(401);
        /**
         * {@code 402 Payment Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.3">HTTP/1.1</a>
         */
        public static final Status PAYMENT_REQUIRED = new Status(402);
        /**
         * {@code 403 Forbidden}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.4">HTTP/1.1</a>
         */
        public static final Status FORBIDDEN = new Status(403);
        /**
         * {@code 404 Not Found}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.5">HTTP/1.1</a>
         */
        public static final Status NOT_FOUND = new Status(404);
        /**
         * {@code 405 Method Not Allowed}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.6">HTTP/1.1</a>
         */
        public static final Status METHOD_NOT_ALLOWED = new Status(405);
        /**
         * {@code 406 Not Acceptable}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.7">HTTP/1.1</a>
         */
        public static final Status NOT_ACCEPTABLE = new Status(406);
        /**
         * {@code 407 Proxy Authentication Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.8">HTTP/1.1</a>
         */
        public static final Status PROXY_AUTHENTICATION_REQUIRED = new Status(407);
        /**
         * {@code 408 Request Timeout}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.9">HTTP/1.1</a>
         */
        public static final Status REQUEST_TIMEOUT = new Status(408);
        /**
         * {@code 409 Conflict}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.10">HTTP/1.1</a>
         */
        public static final Status CONFLICT = new Status(409);
        /**
         * {@code 410 Gone}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.11">HTTP/1.1</a>
         */
        public static final Status GONE = new Status(410);
        /**
         * {@code 411 Length Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.12">HTTP/1.1</a>
         */
        public static final Status LENGTH_REQUIRED = new Status(411);
        /**
         * {@code 412 Precondition failed}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.13">HTTP/1.1</a>
         */
        public static final Status PRECONDITION_FAILED = new Status(412);
        /**
         * {@code 413 Request Entity Too Large}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1</a>
         */
        public static final Status REQUEST_ENTITY_TOO_LARGE = new Status(413);
        /**
         * {@code 414 Request-URI Too Long}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1</a>
         */
        public static final Status REQUEST_URI_TOO_LONG = new Status(414);
        /**
         * {@code 415 Unsupported Media Type}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.16">HTTP/1.1</a>
         */
        public static final Status UNSUPPORTED_MEDIA_TYPE = new Status(415);
        /**
         * {@code 416 Requested Range Not Satisfiable}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.17">HTTP/1.1</a>
         */
        public static final Status REQUESTED_RANGE_NOT_SATISFIABLE = new Status(416);
        /**
         * {@code 417 Expectation Failed}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.18">HTTP/1.1</a>
         */
        public static final Status EXPECTATION_FAILED = new Status(417);
        /**
         * {@code 418 I'm a teapot}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
         */
        public static final Status I_AM_A_TEAPOT = new Status(418);
        /**
         * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
         */
        @Deprecated
        public static final Status INSUFFICIENT_SPACE_ON_RESOURCE = new Status(419);
        /**
         * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
         */
        @Deprecated
        public static final Status METHOD_FAILURE = new Status(420);
        /**
         * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
         */
        @Deprecated
        public static final Status DESTINATION_LOCKED = new Status(421);
        /**
         * {@code 422 Unprocessable Entity}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
         */
        public static final Status UNPROCESSABLE_ENTITY = new Status(422);
        /**
         * {@code 423 Locked}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
         */
        public static final Status LOCKED = new Status(423);
        /**
         * {@code 424 Failed Dependency}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
         */
        public static final Status FAILED_DEPENDENCY = new Status(424);
        /**
         * {@code 426 Upgrade Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS Within HTTP/1.1</a>
         */
        public static final Status UPGRADE_REQUIRED = new Status(426);
        /**
         * {@code 428 Precondition Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
         */
        public static final Status PRECONDITION_REQUIRED = new Status(428);
        /**
         * {@code 429 Too Many Requests}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
         */
        public static final Status TOO_MANY_REQUESTS = new Status(429);
        /**
         * {@code 431 Request Header Fields Too Large}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
         */
        public static final Status REQUEST_HEADER_FIELDS_TOO_LARGE = new Status(431);

        // --- 5xx Server Error ---

        /**
         * {@code 500 Internal Server Error}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.1">HTTP/1.1</a>
         */
        public static final Status INTERNAL_SERVER_ERROR = new Status(500);
        /**
         * {@code 501 Not Implemented}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.2">HTTP/1.1</a>
         */
        public static final Status NOT_IMPLEMENTED = new Status(501);
        /**
         * {@code 502 Bad Gateway}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.3">HTTP/1.1</a>
         */
        public static final Status BAD_GATEWAY = new Status(502);
        /**
         * {@code 503 Service Unavailable}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.4">HTTP/1.1</a>
         */
        public static final Status SERVICE_UNAVAILABLE = new Status(503);
        /**
         * {@code 504 Gateway Timeout}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.5">HTTP/1.1</a>
         */
        public static final Status GATEWAY_TIMEOUT = new Status(504);
        /**
         * {@code 505 HTTP Version Not Supported}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.6">HTTP/1.1</a>
         */
        public static final Status HTTP_VERSION_NOT_SUPPORTED = new Status(505);
        /**
         * {@code 506 Variant Also Negotiates}
         *
         * @see <a href="http://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
         */
        public static final Status VARIANT_ALSO_NEGOTIATES = new Status(506);
        /**
         * {@code 507 Insufficient Storage}
         *
         * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
         */
        public static final Status INSUFFICIENT_STORAGE = new Status(507);
        /**
         * {@code 508 Loop Detected}
         *
         * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
         */
        public static final Status LOOP_DETECTED = new Status(508);
        /**
         * {@code 509 Bandwidth Limit Exceeded}
         */
        public static final Status BANDWIDTH_LIMIT_EXCEEDED = new Status(509);
        /**
         * {@code 510 Not Extended}
         *
         * @see <a href="http://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
         */
        public static final Status NOT_EXTENDED = new Status(510);
        /**
         * {@code 511 Network Authentication Required}.
         *
         * @see <a href="http://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
         */
        public static final Status NETWORK_AUTHENTICATION_REQUIRED = new Status(511);

    }

    public static Status status(int n) {
        return Status.valueOf(n);
    }

    public static final class Header implements Serializable {

        private static final long serialVersionUID = -3987421318751857114L;

        public static final class Names {
            /**
             * {@code "Accept"}
             */
            public static final String ACCEPT = "Accept";
            /**
             * {@code "Accept-Charset"}
             */
            public static final String ACCEPT_CHARSET = "Accept-Charset";
            /**
             * {@code "Accept-Encoding"}
             */
            public static final String ACCEPT_ENCODING = "Accept-Encoding";
            /**
             * {@code "Accept-Language"}
             */
            public static final String ACCEPT_LANGUAGE = "Accept-Language";
            /**
             * {@code "Accept-Ranges"}
             */
            public static final String ACCEPT_RANGES = "Accept-Ranges";
            /**
             * {@code "Accept-Patch"}
             */
            public static final String ACCEPT_PATCH = "Accept-Patch";
            /**
             * {@code "Access-Control-Allow-Origin"}
             */
            public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
            /**
             * {@code "Access-Control-Allow-Methods"}
             */
            public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
            /**
             * {@code "Access-Control-Allow-Headers"}
             */
            public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
            /**
             * {@code "Access-Control-Allow-Credentials"}
             */
            public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
            /**
             * {@code "Access-Control-Expose-Headers"}
             */
            public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
            /**
             * {@code "Access-Control-Max-Age"}
             */
            public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
            /**
             * {@code "Access-Control-Request-Method"}
             */
            public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
            /**
             * {@code "Access-Control-Request-Headers"}
             */
            public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

            /**
             * {@code "Age"}
             */
            public static final String AGE = "Age";
            /**
             * {@code "Allow"}
             */
            public static final String ALLOW = "Allow";
            /**
             * {@code "Authorization"}
             */
            public static final String AUTHORIZATION = "Authorization";
            /**
             * {@code "Cache-Control"}
             */
            public static final String CACHE_CONTROL = "Cache-Control";
            /**
             * {@code "Connection"}
             */
            public static final String CONNECTION = "Connection";
            /**
             * {@code "Content-Base"}
             */
            public static final String CONTENT_BASE = "Content-Base";
            /**
             * {@code "Content-Disposition"}
             */
            public static final String CONTENT_DISPOSITION = "Content-Disposition";
            /**
             * {@code "Content-Encoding"}
             */
            public static final String CONTENT_ENCODING = "Content-Encoding";
            /**
             * {@code "Content-Language"}
             */
            public static final String CONTENT_LANGUAGE = "Content-Language";
            /**
             * {@code "Content-Length"}
             */
            public static final String CONTENT_LENGTH = "Content-Length";
            /**
             * {@code "Content-Location"}
             */
            public static final String CONTENT_LOCATION = "Content-Location";
            /**
             * {@code "Content-Transfer-Encoding"}
             */
            public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
            /**
             * {@code "Content-MD5"}
             */
            public static final String CONTENT_MD5 = "Content-Md5";
            /**
             * {@code "Content-Range"}
             */
            public static final String CONTENT_RANGE = "Content-Range";
            /**
             * {@code "Content-Type"}
             */
            public static final String CONTENT_TYPE = "Content-Type";
            /**
             * {@code "Cookie"}
             */
            public static final String COOKIE = "Cookie";
            /**
             * {@code "Date"}
             */
            public static final String DATE = "Date";
            /**
             * {@code "ETag"}
             */
            public static final String ETAG = "Etag";
            /**
             * {@code "Expect"}
             */
            public static final String EXPECT = "Expect";
            /**
             * {@code "Expires"}
             */
            public static final String EXPIRES = "Expires";
            /**
             * {@code "From"}
             */
            public static final String FROM = "From";
            /**
             * {@code "Front-End-Https"}
             */
            public static final String FRONT_END_HTTPS = "Front-End-Https";
            /**
             * {@code "Host"}
             */
            public static final String HOST = "Host";
            /**
             * {@code "HTTP_CLIENT_IP"}
             */
            public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
            /**
             * {@code "HTTP_X_FORWARDED_FOR"}
             */
            public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
            /**
             * {@code "If-Match"}
             */
            public static final String IF_MATCH = "If-Match";
            /**
             * {@code "If-Modified-Since"}
             */
            public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
            /**
             * {@code "If-None-Match"}
             */
            public static final String IF_NONE_MATCH = "If-None-Match";
            /**
             * {@code "If-Range"}
             */
            public static final String IF_RANGE = "If-Range";
            /**
             * {@code "If-Unmodified-Since"}
             */
            public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
            /**
             * {@code "Last-Modified"}
             */
            public static final String LAST_MODIFIED = "Last-Modified";
            /**
             * {@code "Location"}
             */
            public static final String LOCATION = "Location";
            /**
             * {@code "Max-Forwards"}
             */
            public static final String MAX_FORWARDS = "Max-Forwards";
            /**
             * {@code "Origin"}
             */
            public static final String ORIGIN = "Origin";
            /**
             * {@code "Pragma"}
             */
            public static final String PRAGMA = "Pragma";
            /**
             * {@code "Proxy-Authenticate"}
             */
            public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
            /**
             * {@code "Proxy-Authorization"}
             */
            public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
            /**
             * {@code "Proxy-Client-IP"}
             */
            public static final String PROXY_CLIENT_IP = "Proxy-Client-ip";
            /**
             * {@code "Proxy-Connection"}
             */
            public static final String PROXY_CONNECTION = "Proxy_Connection";
            /**
             * {@code "Range"}
             */
            public static final String RANGE = "Range";
            /**
             * {@code "Referer"}
             */
            public static final String REFERER = "Referer";
            /**
             * {@code "Retry-After"}
             */
            public static final String RETRY_AFTER = "Retry-After";
            /**
             * the header used to put the real ip by load balancers like F5
             * {@code "rlnclientipaddr"}
             */
            public static final String RLNCLIENTIPADDR = "rlnclientipaddr";
            /**
             * {@code "Sec-Websocket-Key1"}
             */
            public static final String SEC_WEBSOCKET_KEY1 = "Sec-Websocket-Key1";
            /**
             * {@code "Sec-Websocket-Key2"}
             */
            public static final String SEC_WEBSOCKET_KEY2 = "Sec-Websocket-Key2";
            /**
             * {@code "Sec-Websocket-Location"}
             */
            public static final String SEC_WEBSOCKET_LOCATION = "Sec-Websocket-Location";
            /**
             * {@code "Sec-Websocket-Origin"}
             */
            public static final String SEC_WEBSOCKET_ORIGIN = "Sec-Websocket-Rrigin";
            /**
             * {@code "Sec-Websocket-Protocol"}
             */
            public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-Websocket-Protocol";
            /**
             * {@code "Sec-Websocket-Version"}
             */
            public static final String SEC_WEBSOCKET_VERSION = "Sec-Websocket-Version";
            /**
             * {@code "Sec-Websocket-Key"}
             */
            public static final String SEC_WEBSOCKET_KEY = "Sec-Websocket-Key";
            /**
             * {@code "Sec-Websocket-Accept"}
             */
            public static final String SEC_WEBSOCKET_ACCEPT = "Sec-Websocket-Accept";
            /**
             * {@code "Server"}
             */
            public static final String SERVER = "Server";
            /**
             * {@code "Set-Cookie"}
             */
            public static final String SET_COOKIE = "Set-Cookie";
            /**
             * {@code "Set-Cookie2"}
             */
            public static final String SET_COOKIE2 = "Set-Cookie2";
            /**
             * {@code "TE"}
             */
            public static final String TE = "TE";
            /**
             * {@code "Trailer"}
             */
            public static final String TRAILER = "Trailer";
            /**
             * {@code "Transfer-Encoding"}
             */
            public static final String TRANSFER_ENCODING = "Transfer-Encoding";
            /**
             * {@code "Upgrade"}
             */
            public static final String UPGRADE = "Upgrade";
            /**
             * {@code "User-Agent"}
             */
            public static final String USER_AGENT = "User-Agent";
            /**
             * {@code "Vary"}
             */
            public static final String VARY = "Vary";
            /**
             * {@code "Via"}
             */
            public static final String VIA = "Via";
            /**
             * {@code "Warning"}
             */
            public static final String WARNING = "Warning";
            /**
             * {@code "WebSocket-Location"}
             */
            public static final String WEBSOCKET_LOCATION = "Websocket-Location";
            /**
             * {@code "WebSocket-Origin"}
             */
            public static final String WEBSOCKET_ORIGIN = "Webwocket-Origin";
            /**
             * {@code "WebSocket-Protocol"}
             */
            public static final String WEBSOCKET_PROTOCOL = "Websocket-Protocol";
            /**
             * {@code "WL-Proxy-Client-IP"}
             */
            public static final String WL_PROXY_CLIENT_IP = "Wl-Proxy-Client-Ip";
            /**
             * {@code "WWW-Authenticate"}
             */
            public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

            /**
             * {@code "X_Requested_With"}
             */
            public static final String X_REQUESTED_WITH = "X-Requested-With";

            /**
             * {@code "X-Forwarded-Host"}
             */
            public static final String X_FORWARDED_HOST = "X-Forwarded-Host";

            /**
             * {@code "X_Forwared_For"}
             */
            public static final String X_FORWARDED_FOR = "X-Forwarded-For";

            /**
             * {@code "X_Forwared_Proto"}
             */
            public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
            /**
             * {@code "X-Forwarded-Ssl"}
             */
            public static final String X_FORWARDED_SSL = "X-Forwarded-Ssl";

            /**
             * {@code "X-Http-Method-Override"}
             */
            public static final String X_HTTP_METHOD_OVERRIDE = "X-Http-Method-Override";
            /**
             * {@code "X-Url-Scheme"}
             */
            public static final String X_URL_SCHEME = "X-Url-Scheme";
            /**
             * {@code "X-Xsrf-Token"}
             */
            public static final String X_XSRF_TOKEN = "X-Xsrf-Token";

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
    public static class Format implements Serializable {

        private static final Map<String, Format> predefined = new LinkedHashMap<String, Format>();

        private int ordinal;
        private String name;
        private String contentType;

        private Format(String name, String contentType) {
            this(name, contentType, true);
        }

        private Format(String name, String contentType, boolean predefined) {
            this.name = name.toLowerCase();
            this.contentType = contentType;
            if (predefined) {
                Format.predefined.put(name, this);
                this.ordinal = ordinal(name);
            } else {
                this.ordinal = -1;
            }
        }

        public final String name() {
            return name;
        }

        public final int ordinal() {
            return ordinal;
        }

        /**
         * Returns the content type string
         *
         * @return the content type string of this format
         */
        public String contentType() {
            return contentType;
        }

        /**
         * Deprecated. Please use {@link #contentType()}
         * @return the content type string of the format
         */
        @Deprecated
        public final String toContentType() {
            return contentType();
        }

        public final String getName() {
            return name();
        }

        public final String getContentType() {
            return contentType();
        }

        public boolean isText() {
            return JSON == this || contentType.startsWith("text/") || S.eq("application/json", contentType);
        }

        /**
         * Returns the error message
         *
         * @param message the message
         * @return the message directly
         */
        public String errorMessage(String message) {
            return message;
        }

        @Override
        public int hashCode() {
            if (ordinal != -1) {
                return ordinal;
            }
            return $.hc(name, contentType);
        }

        @Override
        public String toString() {
            return name();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Format) {
                Format that = (Format) obj;
                return $.eq(that.name, this.name) && $.eq(that.contentType, this.contentType);
            }
            return false;
        }

        private Object readResolve() {
            if (ordinal == -1) {
                return this;
            }
            return predefined.get(name);
        }

        /**
         * Deprecated. please Use {@link #predefined()}
         * @return an array of predefined Formats
         */
        public static Format[] values() {
            Format[] retVal = new Format[predefined.size()];
            return predefined.values().toArray(retVal);
        }

        public static List<Format> predefined() {
            return C.list(predefined.values());
        }

        public static Format of(String name) {
            return valueOf(name);
        }

        public static Format of(String name, String contentType) {
            return valueOf(name, contentType);
        }

        public static Format valueOf(String name) {
            name = name.toLowerCase();
            if (name.startsWith(".")) {
                name = S.afterLast(name, ".");
            }
            return predefined.get(name.toLowerCase());
        }

        public static Format valueOf(String name, String contentType) {
            Format retVal = valueOf(name);
            if (null != retVal) {
                return retVal;
            }
            E.illegalArgumentIf(S.blank(name), "name cannot be blank string");
            E.illegalArgumentIf(S.blank(contentType), "content type cannot be blank string");
            name = name.toLowerCase();
            if (name.startsWith(".")) {
                name = S.afterLast(name, ".");
            }
            return new Format(name, contentType, false);
        }

        public static Format resolve(Format def, String accept) {
            E.NPE(def);
            return resolve_(def, accept);
        }

        public static Format resolve(Iterable<String> accepts) {
            return resolve(Format.HTML, accepts);
        }

        public static Format resolve(Format def, Iterable<String> accepts) {
            Format retVal;
            for (String s : accepts) {
                retVal = resolve_(null, s);
                if (null != retVal) {
                    return retVal;
                }
            }
            return $.ifNullThen(def, Format.HTML);
        }

        public static Format resolve(String... accepts) {
            return resolve(Format.HTML, accepts);
        }

        public static Format resolve(Format def, String... accepts) {
            Format retVal;
            for (String s : accepts) {
                retVal = resolve_(null, s);
                if (null != retVal) {
                    return retVal;
                }
            }
            return $.ifNullThen(def, Format.HTML);
        }

        /**
         * Resolve {@code Format} instance out of an http "Accept" header.
         *
         * @param accept the value of http "Accept" header
         * @return an {@code Format} instance
         */
        public static Format resolve(String accept) {
            return resolve_(Format.UNKNOWN, accept);
        }


        public static String toContentType(String fmt) {
            Format f = predefined.get(fmt.toLowerCase());
            if (null == f) {
                f = HTML;
            }
            return f.contentType();
        }

        private static int ordinal(String s) {
            int l = s.length(), h = 0;
            for (int i = 0; i < l; ++i) {
                char c = s.charAt(i);
                h = 31 * h + c;
            }
            return h;
        }

        private static Format resolve_(Format def, String contentType) {
            Format fmt = def;
            if (S.blank(contentType)) {
                fmt = HTML;
            } else if (contentType.contains("application/xhtml") || contentType.contains("text/html") || contentType.startsWith("*/*")) {
                fmt = HTML;
            } else if (contentType.contains("application/xml") || contentType.contains("text/xml")) {
                fmt = XML;
            } else if (contentType.contains("application/json") || contentType.contains("text/javascript")) {
                fmt = JSON;
            } else if (contentType.contains("application/x-www-form-urlencoded")) {
                fmt = FORM_URL_ENCODED;
            } else if (contentType.contains("multipart/form-data") || contentType.contains("multipart/mixed")) {
                fmt = FORM_MULTIPART_DATA;
            } else if (contentType.contains("text/plain")) {
                fmt = TXT;
            } else if (contentType.contains("csv") || contentType.contains("comma-separated-values")) {
                fmt = CSV;
            } else if (contentType.contains("ms-excel")) {
                fmt = XLS;
            } else if (contentType.contains("spreadsheetml")) {
                fmt = XLSX;
            } else if (contentType.contains("pdf")) {
                fmt = PDF;
            } else if (contentType.contains("msword")) {
                fmt = DOC;
            } else if (contentType.contains("wordprocessingml")) {
                fmt = DOCX;
            } else if (contentType.contains("rtf")) {
                fmt = RTF;
            }

            return fmt;
        }

        static {
            try {
                InputStream is = H.class.getResourceAsStream("mime-types.properties");
                Properties types = new Properties();
                types.load(is);
                for (Object k : types.keySet()) {
                    String fmt = k.toString();
                    String contentType = types.getProperty(fmt);
                    new Format(fmt, contentType);
                }
            } catch (IOException e) {
                throw E.ioException(e);
            }
        }

        /**
         * The "text/html" content format
         */
        public static final Format HTML = valueOf("html");
        /**
         * Deprecated, please use {@link #HTML}
         */
        @Deprecated
        public static final Format html = HTML;

        /**
         * The "text/xml" content format
         */
        public static final Format XML = valueOf("xml");
        /**
         * Deprecated, please use {@link #XML}
         */
        @Deprecated
        public static final Format xml = XML;

        /**
         * The "application/json" content format
         */
        public static final Format JSON = new Format("json", "application/json") {
            @Override
            public String errorMessage(String message) {
                return S.fmt("{\"error\": \"%s\"}", message);
            }
        };
        /**
         * Deprecated. Please use {@link #JSON}
         */
        @Deprecated
        public static final Format json = JSON;

        /**
         * The "text/css" content format
         */
        public static final Format CSS = new Format("css", "text/css");

        /**
         * The "application/javascript" content format
         */
        public static final Format JAVASCRIPT = new Format("javascript", "application/javascript") {
            @Override
            public String errorMessage(String message) {
                return "alert(" + message + ");";
            }
        };

        /**
         * The "application/vnd.ms-excel" content format
         */
        public static final Format XLS = valueOf("xls");
        /**
         * Deprecated. Please use {@link #XLS}
         */
        public static final Format xls = XLS;

        /**
         * The "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" content format
         */
        public static final Format XLSX = valueOf("xlsx");
        /**
         * Deprecated. Please use {@link #XLSX}
         */
        public static final Format xlsx = XLSX;

        /**
         * The "application/vnd.ms-word" content format
         */
        public static final Format DOC = valueOf("doc");

        /**
         * Deprecated. Please use {@link #DOC}
         */
        public static final Format doc = DOC;

        /**
         * The "application/vnd.openxmlformats-officedocument.wordprocessingml.document" content format
         */
        public static final Format DOCX = valueOf("docx");

        /**
         * Deprecated. Please use {@link #DOCX}
         */
        public static final Format docx = DOCX;

        /**
         * The "text/csv" content format
         */
        public static final Format CSV = valueOf("csv");

        /**
         * Deprecated, please use {@link #CSV}
         */
        @Deprecated
        public static final Format csv = CSV;

        /**
         * The "text/plain" content format
         */
        public static final Format TXT = valueOf("txt");

        /**
         * Deprecated, please use {@link #TXT}
         */
        @Deprecated
        public static final Format txt = TXT;

        /**
         * The "application/pdf" content format
         */
        public static final Format PDF = valueOf("pdf");
        /**
         * Deprecated, please use {@link #PDF}
         */
        @Deprecated
        public static final Format pdf = PDF;

        /**
         * The "application/rtf" content format
         */
        public static final Format RTF = valueOf("rtf");
        /**
         * Deprecated, please use {@link #RTF}
         */
        @Deprecated
        public static final Format rtf = RTF;

        // --- common images
        public static final Format GIF = valueOf("gif");
        public static final Format ICO = valueOf("ico");
        public static final Format JPG = valueOf("jpg");
        public static final Format BMP = valueOf("bmp");
        public static final Format PNG = valueOf("png");
        public static final Format SVG = valueOf("svg");
        public static final Format TIF = valueOf("tif");

        // -- common videos
        public static final Format MOV = valueOf("mov");
        public static final Format MP4 = valueOf("mp4");
        public static final Format MPG = valueOf("mpg");
        public static final Format AVI = valueOf("avi");
        public static final Format FLV = valueOf("flv");

        // -- common audios
        public static final Format MP3 = valueOf("mp3");
        public static final Format MPA = valueOf("mpa");
        public static final Format WAV = valueOf("wav");


        /**
         * The "application/x-www-form-urlencoded" content format
         */
        public static final Format FORM_URL_ENCODED = new Format("form_url_encoded", "application/x-www-form-urlencoded");
        /**
         * Deprecated, please use {@link #FORM_URL_ENCODED}
         */
        @Deprecated
        public static final Format form_url_encoded = FORM_URL_ENCODED;

        /**
         * The "multipart/form-data" content format
         */
        public static final Format FORM_MULTIPART_DATA = new Format("form_multipart_data", "multipart/form-data");
        /**
         * Deprecated, please use {@link #FORM_MULTIPART_DATA}
         */
        @Deprecated
        public static final Format form_multipart_data = FORM_MULTIPART_DATA;

        public static final Format BINARY = new Format("binary", "application/octet-stream");

        /**
         * The "unknown" content format. Use default content type: "text/html"
         */
        public static final Format UNKNOWN = new Format("unknown", "text/html") {
            @Override
            public String contentType() {
                String s = Current.format();
                if (!S.blank(s)) {
                    return toContentType(s);
                }
                return "text/html";
            }

            @Override
            public String toString() {
                String s = Current.format();
                return null == s ? name() : s;
            }
        };
        /**
         * Deprecated, please use {@link #UNKNOWN}
         */
        @Deprecated
        public static final Format unknown = UNKNOWN;

        public static final class Ordinal {
            public static final int HTML = Format.HTML.ordinal;
            public static final int XML = Format.XML.ordinal;
            public static final int JSON = Format.JSON.ordinal;
            public static final int XLS = Format.XLS.ordinal;
            public static final int XLSX = Format.XLSX.ordinal;
            public static final int DOC = Format.DOC.ordinal;
            public static final int DOCX = Format.DOCX.ordinal;
            public static final int CSV = Format.CSV.ordinal;
            public static final int TXT = Format.TXT.ordinal;
            public static final int PDF = Format.PDF.ordinal;
            public static final int RTF = Format.RTF.ordinal;
            public static final int GIF = Format.GIF.ordinal;
            public static final int ICO = Format.ICO.ordinal;
            public static final int JPG = Format.JPG.ordinal;
            public static final int BMP = Format.BMP.ordinal;
            public static final int PNG = Format.PNG.ordinal;
            public static final int SVG = Format.SVG.ordinal;
            public static final int TIF = Format.TIF.ordinal;
            public static final int MOV = Format.MOV.ordinal;
            public static final int MP4 = Format.MP4.ordinal;
            public static final int MPG = Format.MPG.ordinal;
            public static final int AVI = Format.AVI.ordinal;
            public static final int FLV = Format.FLV.ordinal;
            public static final int MP3 = Format.MP3.ordinal;
            public static final int MPA = Format.MPA.ordinal;
            public static final int WAV = Format.WAV.ordinal;
            public static final int FORM_URL_ENCODED = Format.FORM_URL_ENCODED.ordinal;
            public static final int FORM_MULTIPART_DATA = Format.FORM_MULTIPART_DATA.ordinal;
        }
    }

    public enum MediaType {
        CSS, CSV, DOC, DOCX, HTML, JAVASCRIPT, JSON, PDF, TXT, XLS, XLSX, XML;
        private Format fmt;
        private MediaType() {
            fmt = Format.valueOf(name());
        }

        public Format format() {
            return fmt;
        }

        @Override
        public String toString() {
            return fmt.contentType();
        }
    }

    public static Format format(String name) {
        return Format.valueOf(name);
    }

    public static Format format(String name, String contentType) {
        return Format.valueOf(name, contentType);
    }

    /**
     * The HTTP cookie
     */
    public static class Cookie implements Serializable {

        private static final long serialVersionUID = 5325872881041347558L;

        private String name;

        // default is non-persistent cookie
        private int maxAge = -1;

        private boolean secure;

        private String path;

        private String domain;

        private String value;

        private boolean httpOnly;

        private int version;

        private Date expires;

        private String comment;

        public Cookie(String name) {
            this(name, "");
        }

        public Cookie(String name, String value) {
            E.NPE(name);
            this.name = name;
            this.value = null == value ? "" : value;
        }

        public Cookie(String name, String value, int maxAge, boolean secure, String path, String domain, boolean httpOnly) {
            this(name, value);
            this.maxAge = maxAge;
            this.secure = secure;
            this.path = path;
            this.domain = domain;
            this.httpOnly = httpOnly;
        }

        /**
         * Returns the name of the cookie. Cookie name
         * cannot be changed after created
         *
         * @return the name
         */
        public String name() {
            return name;
        }

        /**
         * Returns the value of the cookie
         * @return the value
         */
        public String value() {
            return value;
        }

        /**
         * Set a value to a cookie and the return {@code this} cookie
         *
         * @param value the value to be set to the cookie
         * @return this cookie
         */
        public Cookie value(String value) {
            this.value = value;
            return this;
        }

        /**
         * Returns the domain of the cookie
         *
         * @return domain
         */
        public String domain() {
            return domain;
        }

        /**
         * Set the domain of the cookie
         *
         * @param domain the  domain string
         * @return this cookie
         */
        public Cookie domain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Returns the path on the server
         * to which the browser returns this cookie. The
         * cookie is visible to all subpaths on the server.
         *
         * @return the path
         * @see #path(String)
         */
        public String path() {
            return path;
        }

        /**
         * Specifies a path for the cookie
         * to which the client should return the cookie.
         * 
         * <p>The cookie is visible to all the pages in the directory
         * you specify, and all the pages in that directory's subdirectories.
         * 
         * <p>Consult RFC 2109 (available on the Internet) for more
         * information on setting path names for cookies.
         *
         * @param uri a <code>String</code> specifying a path
         * @return this cookie after path is set
         * @see #path
         */
        public Cookie path(String uri) {
            this.path = uri;
            return this;
        }

        /**
         * Returns the maximum age of cookie specified in seconds. If
         * maxAge is set to {@code -1} then the cookie will persist until
         * browser shutdown
         *
         * @return the max age of the cookie
         */
        public int maxAge() {
            return maxAge;
        }

        /**
         * Set the max age of the cookie in seconds.
         * <p>A positive value indicates that the cookie will expire
         * after that many seconds have passed. Note that the value is
         * the <i>maximum</i> age when the cookie will expire, not the cookie's
         * current age.
         * 
         * <p>A negative value means
         * that the cookie is not stored persistently and will be deleted
         * when the Web browser exits. A zero value causes the cookie
         * to be deleted.
         *
         * @param maxAge the max age to be set
         * @return this instance
         * @see #maxAge()
         */
        public Cookie maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Date expires() {
            if (null != expires) {
                return expires;
            }
            if (maxAge < 0) {
                return null;
            }
            return new Date($.ms() + maxAge * 1000);
        }

        public Cookie expires(Date expires) {
            this.expires = expires;
            if (null != expires && -1 == maxAge) {
                maxAge = (int) ((expires.getTime() - $.ms()) / 1000);
            }
            return this;
        }

        /**
         * Returns <code>true</code> if the browser is sending cookies
         * only over a secure protocol, or <code>false</code> if the
         * browser can send cookies using any protocol.
         *
         * @return if the cookie is secure
         * @see #secure(boolean)
         */
        public boolean secure() {
            return secure;
        }

        /**
         * Indicates to the browser whether the cookie should only be sent
         * using a secure protocol, such as HTTPS or SSL.
         * 
         * <p>The default value is <code>false</code>.
         *
         * @param secure the cookie secure requirement
         * @return this cookie instance
         */
        public Cookie secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Returns the version of the protocol this cookie complies
         * with. Version 1 complies with RFC 2109,
         * and version 0 complies with the original
         * cookie specification drafted by Netscape. Cookies provided
         * by a browser use and identify the browser's cookie version.
         *
         * @return 0 if the cookie complies with the
         * original Netscape specification; 1
         * if the cookie complies with RFC 2109
         * @see #version(int)
         */
        public int version() {
            return version;
        }

        /**
         * Sets the version of the cookie protocol that this Cookie complies
         * with.
         * 
         * <p>Version 0 complies with the original Netscape cookie
         * specification. Version 1 complies with RFC 2109.
         * 
         * <p>Since RFC 2109 is still somewhat new, consider
         * version 1 as experimental; do not use it yet on production sites.
         *
         * @param v 0 if the cookie should comply with the original Netscape
         *          specification; 1 if the cookie should comply with RFC 2109
         * @return this cookie instance
         * @see #version()
         */
        public Cookie version(int v) {
            this.version = v;
            return this;
        }

        public boolean httpOnly() {
            return httpOnly;
        }

        public Cookie httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public String comment() {
            return comment;
        }

        public Cookie comment(String comment) {
            this.comment = comment;
            return this;
        }

        private static void ensureInit() {
            if (!Current.cookieMapInitialized()) {
                Request req = Request.current();
                E.illegalStateIf(null == req);
                req._initCookieMap();
            }
        }

        /**
         * Add a cookie to the current context
         *
         * @param cookie the cookie
         */
        public static void set(Cookie cookie) {
            ensureInit();
            Current.setCookie(cookie.name(), cookie);
        }

        /**
         * Get a cookie from current context by name
         *
         * @param name the cookie name
         * @return a cookie with the name specified
         */
        public static Cookie get(String name) {
            ensureInit();
            return Current.getCookie(name);
        }

        /**
         * Returns all cookies from current context
         * @return all cookies
         */
        public static Collection<Cookie> all() {
            ensureInit();
            return Current.cookies();
        }

        /**
         * The function object namespace
         */
        public static enum F {
            ;
            public static final $.F2<Cookie, Response, Void> ADD_TO_RESPONSE = new $.F2<Cookie, Response, Void>() {
                @Override
                public Void apply(Cookie cookie, Response response) throws NotAppliedException, $.Break {
                    response.addCookie(cookie);
                    return null;
                }
            };

        }

    } // eof Cookie

    public static class KV<T extends KV> implements Serializable {
        private static final long serialVersionUID = 891504755320699989L;
        protected Map<String, String> data = C.newMap();
        private boolean dirty = false;

        private KV() {}

        private KV(Map<String, String> data) {
            E.NPE(data);
            this.data = data;
        }

        /**
         * Associate a string value with the key specified during
         * initialization. The difference between calling {@code load}
         * and {@link #put(String, String)} is the former will not change
         * the dirty tag
         * @param key the key
         * @param val the value
         * @return this instance
         */
        public T load(String key, String val) {
            E.illegalArgumentIf(key.contains(":"));
            data.put(key, val);
            return me();
        }

        /**
         * Associate a string value with the key specified.
         * @param key the key
         * @param val the value
         * @return this instance
         */
        public T put(String key, String val) {
            E.illegalArgumentIf(key.contains(":"));
            dirty = true;
            return load(key, val);
        }

        /**
         * Associate an Object value's String representation with the
         * key specified. If the object is {@code null} then {@code null}
         * is associated with the key specified
         * @param key the key
         * @param val the value
         * @return this instance
         */
        public T put(String key, Object val) {
            String valStr = null == val ? null : val.toString();
            return put(key, valStr);
        }

        /**
         * Returns the string value associated with the key specified
         * @param key the key
         * @return the value string
         */
        public String get(String key) {
            return data.get(key);
        }

        /**
         * Returns the key set of internal data map
         * @return key set
         */
        public Set<String> keySet() {
            return data.keySet();
        }

        /**
         * Returns {@code true} if internal data map is empty
         * @return if data is empty
         */
        public boolean isEmpty() {
            return data.isEmpty();
        }

        /**
         * Indicate if the KV has been changed
         *
         * @return {@code true} if this instance has been changed
         */
        public boolean dirty() {
            return dirty;
        }

        /**
         * Alias of {@link #dirty()}
         * @return true if data has been changed
         */
        public boolean changed() {
            return dirty;
        }

        /**
         * Returns true if the internal data map is empty
         * @return true if data is empty or false otherwise
         */
        public boolean empty() {
            return data.isEmpty();
        }

        /**
         * Returns true if an association with key specified exists in
         * the internal map
         * @param key the key
         * @return true if data contains key or false otherwise
         */
        public boolean containsKey(String key) {
            return data.containsKey(key);
        }

        /**
         * Alias of {@link #containsKey(String)}
         * @param key  the key
         * @return true if data contains the key or false otherwise
         */
        public boolean contains(String key) {
            return containsKey(key);
        }

        /**
         * Returns the number of assoications stored in the internal map
         * @return size
         */
        public int size() {
            return data.size();
        }

        /**
         * Release an association with key specified
         * @param key specify the k-v pair that should be removed from internal map
         * @return this instance
         */
        public T remove(String key) {
            data.remove(key);
            return me();
        }

        /**
         * Clear the internal data map. In other words, all
         * Key/Value association stored in this instance has been
         * release
         *
         * @return this instance
         */
        public T clear() {
            data.clear();
            return me();
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof KV) {
                KV that = (KV) obj;
                return that.data.equals(this.data);
            }
            return false;
        }

        @Override
        public String toString() {
            return data.toString();
        }

        protected T me() {
            return (T) this;
        }

    }

    /**
     * Defines a data structure to encapsulate a stateless session which
     * accept only {@code String} type value, and will be persisted at
     * client side as a cookie. This means the entire size of the
     * information stored in session including names and values shall
     * not exceed 4096 bytes.
     * 
     * <p>To store typed value or big value, use the cache methods
     * of the session class. However it is subject to the implementation
     * to decide whether cache methods are provided and how it is
     * implemented</p>
     */
    public static final class Session extends KV<Session> {

        /**
         * Session identifier
         */
        public static final String KEY_ID = "___ID";

        /**
         * Stores the expiration date in the session
         */
        public static final String KEY_EXPIRATION = "___TS";

        /**
         * Stores the authenticity token in the session
         */
        public static final String KEY_AUTHENTICITY_TOKEN = "___AT";

        /**
         * Used to mark if a session has just expired
         */
        public static final String KEY_EXPIRE_INDICATOR = "___expired";

        /**
         * Stores the fingerprint to the session
         */
        public static final String KEY_FINGER_PRINT = "__FP";
        private static final long serialVersionUID = -423716328552054481L;

        private String id;

        public Session() {
        }

        /**
         * Returns the session identifier
         * @return id of the session
         */
        public String id() {
            if (null == id) {
                id = data.get(KEY_ID);
                if (null == id) {
                    id = UUID.randomUUID().toString();
                    put(KEY_ID, id());
                }
            }
            return id;
        }

        // ------- regular session attribute operations ---

        /**
         * Returns {@code true} if the session is empty. e.g.
         * does not contain anything else than the timestamp
         */
        public boolean empty() {
            return super.empty() || (containsKey(KEY_EXPIRATION) && size() == 1);
        }

        public boolean isEmpty() {
            return super.empty() || (containsKey(KEY_EXPIRATION) && size() == 1);
        }

        /**
         * Check if the session is expired. A session is considered
         * to be expired if it has a timestamp and the timestamp is
         * non negative number and is less than {@link System#currentTimeMillis()}
         *
         * @return {@code true} if the session is expired
         */
        public boolean expired() {
            long expiry = expiry();
            if (expiry < 0) return false;
            return (expiry < System.currentTimeMillis());
        }

        /**
         * Returns the expiration time in milliseconds of this session. If
         * there is no expiration set up, then this method return {@code -1}
         *
         * @return the difference, measured in milliseconds, between
         * the expiry of the session and midnight, January 1,
         * 1970 UTC, or {@code -1} if the session has no
         * expiry
         */
        public long expiry() {
            String s = get(KEY_EXPIRATION);
            if (S.blank(s)) return -1;
            return Long.parseLong(s);
        }

        /**
         * Set session expiry in milliseconds
         *
         * @param expiry the difference, measured in milliseconds, between
         *               the expiry and midnight, January 1, 1970 UTC.
         * @return the session instance
         */
        public Session expireOn(long expiry) {
            put(KEY_EXPIRATION, S.string(expiry));
            return this;
        }

        // ------- eof regular session attribute operations ---

        @Override
        public boolean equals(Object obj) {
            boolean superEq = super.equals(obj);
            return superEq && (obj instanceof H.Session) && $.eq(((Session) obj).id, id);
        }


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
                    cs = HttpConfig.sessionCache();
                }
                return cs;
            }
        }

        /**
         * Store an object into cache using key specified. The key will be
         * appended with session id, so that it distinct between caching
         * using the same key but in different user sessions.
         * 
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
         *
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
         * if it cannot find the object by key
         * specified
         * @see #cache(String, Object)
         */
        public <T> T cached(String key) {
            return cs().get(k(key));
        }

        /**
         * Retrieve an object from cache by key. The key
         * will be attached with session id
         *
         * @param key the key to get the cached object
         * @param clz the class to specify the return type
         * @param <T> the object type
         * @return the object in the cache, or {@code null}
         * if it cannot find the object by key
         * specified
         * @see #cache(String, Object)
         */
        public <T> T cached(String key, Class<T> clz) {
            return cs().get(k(key));
        }
        // ------- eof cache operations ------

        /**
         * Return a session instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         *
         * @return the current session instance
         */
        public static Session current() {
            return Current.session();
        }

        /**
         * Set a session instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         *
         * @param session the session to be set to current execution context
         */
        public static void current(Session session) {
            Current.session(session);
        }

        // used to parse session data persisted in the cookie value
        private static final Pattern _PARSER = Pattern.compile(S.HSEP + "([^:]*):([^" + S.HSEP + "]*)" + S.HSEP);

        /**
         * Resolve a Session instance from a session cookie
         *
         * @param sessionCookie the cookie corresponding to a session
         * @param ttl           session time to live in seconds
         * @return a Session instance
         * @see #serialize(String)
         */
        public static Session resolve(Cookie sessionCookie, int ttl) {
            Session session = new Session();
            long expiration = System.currentTimeMillis() + ttl * 1000;
            boolean hasTtl = ttl > -1;
            String value = null == sessionCookie ? null : sessionCookie.value();
            if (S.blank(value)) {
                if (hasTtl) {
                    session.expireOn(expiration);
                }
            } else {
                int firstDashIndex = value.indexOf("-");
                if (firstDashIndex > -1) {
                    String signature = value.substring(0, firstDashIndex);
                    String data = value.substring(firstDashIndex + 1);
                    if (S.eq(signature, sign(data))) {
                        String sessionData = Codec.decodeUrl(data, Charsets.UTF_8);
                        Matcher matcher = _PARSER.matcher(sessionData);
                        while (matcher.find()) {
                            session.put(matcher.group(1), matcher.group(2));
                        }
                    }
                }
                if (hasTtl && session.expired()) {
                    session = new Session().expireOn(expiration);
                }
            }
            return session;
        }

        /**
         * Serialize this session into a cookie. Note the cookie
         * returned has only name, value maxAge been set. It's up
         * to the caller to set the secure, httpOnly and path
         * attributes
         *
         * @param sessionKey the cookie name for the session cookie
         * @return a cookie captures this session's information or {@code null} if
         * this session is empty or this session hasn't been changed and
         * there is no expiry
         * @see #resolve(org.osgl.http.H.Cookie, int)
         */
        public Cookie serialize(String sessionKey) {
            long expiry = expiry();
            boolean hasTtl = expiry > -1;
            boolean expired = !hasTtl && expiry < System.currentTimeMillis();
            if (!changed() && !hasTtl) return null;
            if (empty() || expired) {
                // empty session, delete the session cookie
                return new H.Cookie(sessionKey).maxAge(0);
            }
            StringBuilder sb = S.builder();
            for (String k : keySet()) {
                sb.append(S.HSEP);
                sb.append(k);
                sb.append(":");
                sb.append(get(k));
                sb.append(S.HSEP);
            }
            String data = Codec.encodeUrl(sb.toString(), Charsets.UTF_8);
            String sign = sign(data);
            String value = S.builder(sign).append("-").append(data).toString();
            Cookie cookie = new Cookie(sessionKey).value(value);

            if (expiry > -1L) {
                int ttl = (int) ((expiry - System.currentTimeMillis()) / 1000);
                cookie.maxAge(ttl);
            }
            return cookie;
        }

        private static String sign(String s) {
            return Crypto.sign(s, s.getBytes(Charsets.UTF_8));
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
    public static final class Flash extends KV<Flash> {

        // used to parse flash data persisted in the cookie value
        private static final Pattern _PARSER = Session._PARSER;
        private static final long serialVersionUID = 5609789840171619780L;

        /**
         * Stores the data that will be output to cookie so next time the user's request income
         * they will be available for the application to access
         */
        private Map<String, String> out = C.newMap();


        /**
         * Add an attribute to the flash scope. The data is
         * added to both data buffer and the out buffer
         *
         * @param key   the key to index the attribute
         * @param value the value of the attribute
         * @return the flash instance
         */
        public Flash put(String key, String value) {
            out.put(key, value);
            return super.put(key, value);
        }

        /**
         * Add an attribute to the flash scope. The value is in Object
         * type, however it will be convert to its {@link Object#toString() string
         * representation} before put into the flash
         *
         * @param key   the key to index the attribute
         * @param value the value to be put into the flash
         * @return this flash instance
         */
        public Flash put(String key, Object value) {
            return put(key, null == value ? null : value.toString());
        }

        /**
         * Add an attribute to the flash's current scope. Meaning when next time
         * the user request to the server, the attribute will not be there anymore.
         *
         * @param key   the attribute key
         * @param value the attribute value
         * @return the flash instance
         */
        public Flash now(String key, String value) {
            return super.put(key, value);
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
         * @param args    the format arguments
         * @return this flash instance
         */
        public Flash error(String message, Object... args) {
            return put("error", S.fmt(message, args));
        }

        /**
         * Get the "error" message that has been added to
         * the flash scope.
         *
         * @return the "error" message or {@code null} if
         * no error message has been added to the flash
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
         * @param args    the format arguments
         * @return this flash instance
         */
        public Flash success(String message, Object... args) {
            return put("success", S.fmt(message, args));
        }

        /**
         * Get the "success" message that has been added to
         * the flash scope.
         *
         * @return the "success" message or {@code null} if
         * no success message has been added to the flash
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
         * @return this flash
         * @see #keep()
         */
        public Flash keep(String key) {
            if (super.containsKey(key)) {
                out.put(key, get(key));
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

        public KV out() {
            return new KV(out);
        }

        /**
         * Return a flash instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         *
         * @return the current flash instance
         */
        public static Flash current() {
            return Current.flash();
        }

        /**
         * Set a flash instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         *
         * @param flash the flash to be set to current execution context
         */
        public static void current(Flash flash) {
            Current.flash(flash);
        }

        /**
         * Resolve a Flash instance from a cookie. If the cookie supplied
         * is {@code null} then an empty Flash instance is returned
         *
         * @param flashCookie the flash cookie
         * @return a Flash instance
         * @see #serialize(String)
         */
        public static Flash resolve(Cookie flashCookie) {
            Flash flash = new Flash();
            if (null != flashCookie) {
                String value = flashCookie.value();
                if (S.notBlank(value)) {
                    String s = Codec.decodeUrl(value, Charsets.UTF_8);
                    Matcher m = _PARSER.matcher(s);
                    while (m.find()) {
                        flash.data.put(m.group(1), m.group(2));
                    }
                }
            }
            return flash;
        }

        /**
         * Serialize this Flash instance into a Cookie. Note
         * the cookie returned has only name, value and max Age
         * been set. It's up to the caller to set secure, path
         * and httpOnly attributes.
         *
         * @param flashKey the cookie name
         * @return a Cookie represent this flash instance
         * @see #resolve(org.osgl.http.H.Cookie)
         */
        public Cookie serialize(String flashKey) {
            if (out.isEmpty()) {
                return new Cookie(flashKey).maxAge(0);
            }
            StringBuilder sb = S.builder();
            for (String key : out.keySet()) {
                sb.append(S.HSEP);
                sb.append(key);
                sb.append(":");
                sb.append(out.get(key));
                sb.append(S.HSEP);
            }
            String value = Codec.encodeUrl(sb.toString(), Charsets.UTF_8);
            return new Cookie(flashKey).value(value);
        }

        @Override
        public boolean equals(Object obj) {
            boolean superEq = super.equals(obj);
            return superEq && (obj instanceof H.Flash);
        }


    } // eof Flash

    /**
     * Defines the HTTP request trait
     *
     * @param <T> the type of the implementation class
     */
    public static abstract class Request<T extends Request> {

        private static SimpleDateFormat dateFormat;

        static {
            dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }


        /**
         * Returns the class of the implementation. Not to be used
         * by application
         * @return the class
         */
        protected abstract Class<T> _impl();

        private Format accept;

        private Format contentType;

        private String ip;

        private int port = -1;

        private State state = State.NONE;

        private Object context;

        private String etag;

        private String referer;

        protected volatile InputStream inputStream;

        protected volatile Reader reader;

        private Map<String, Cookie> cookies = C.newMap();

        /**
         * Attach a context object to the request instance
         * @param context the context object
         * @return the request instance itself
         */
        public T context(Object context) {
            this.context = $.notNull(context);
            return me();
        }

        /**
         * Get the context object from the request instance
         * @param <CONTEXT> the generic type of the context object
         * @return the context object
         */
        public <CONTEXT> CONTEXT context() {
            return (CONTEXT) context;
        }

        /**
         * Returns the HTTP method of the request
         * @return method
         */
        public abstract Method method();

        /**
         * Set the Http method on this request. Used by framework to "override"
         * a HTTP method
         * @param method the method to set
         * @return this request instance
         */
        public abstract T method(Method method);

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
         * Return the request {@link org.osgl.http.H.Format accept}
         *
         * @return the request accept
         */
        public Format accept() {
            if (null == accept) {
                resolveAcceptFormat();
            }
            return accept;
        }

        /**
         * Set {@link org.osgl.http.H.Format accept} to the request
         * @param fmt the format to be set
         * @return this request
         */
        public T accept(Format fmt) {
            this.accept = $.notNull(fmt);
            return me();
        }

        public T accept(MediaType mediaType) {
            this.accept = mediaType.format();
            return me();
        }

        /**
         * Return the "referer(sic)" header value
         * @return the http referrer
         */
        public String referrer() {
            if (null == referer) {
                referer = header(REFERER);
                if (null == referer) {
                    referer = "";
                }
            }
            return referer;
        }

        /**
         * This method is an alias of {@link #referer} which follows the
         * HTTP misspelling header name `referer`
         * @return the http referer
         */
        public String referer() {
            return referrer();
        }

        public String etag() {
            if (null == etag) {
                etag = method().safe() ? header(IF_NONE_MATCH) : header(IF_MATCH);
            }
            return etag;
        }

        public boolean etagMatches(String etag) {
            String etag0 = this.etag();
            return null != etag0 && S.eq(etag0, etag);
        }

        /**
         * Check if the request is an ajax call
         *
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
         * 
         * <p>
         * The path starts with "/" but not end with "/"
         * </p>
         *
         * @return URL path
         */
        public abstract String path();

        /**
         * Returns the context path of the request.
         * The context path starts with "/" but not end
         * with "/". If there is no context path
         * then and empty "" is returned
         * @return context path
         */
        public abstract String contextPath();

        /**
         * Returns the full URI path. It's composed of
         * {@link #contextPath()} and {@link #path()}
         * The full path starts with "/"
         * @return full path
         */
        public String fullPath() {
            return Path.url(path(), this);
        }

        /**
         * Alias of {@link #fullPath()}
         *
         * @return the full URL path of the request
         */
        public String url() {
            return fullPath();
        }

        /**
         * Returns the full URL including scheme, domain, port and
         * full request path plus query string
         *
         * @return the absolute URL
         */
        public String fullUrl() {
            return Path.fullUrl(path(), this);
        }

        /**
         * Returns query string or an empty String if the request
         * doesn't contains a query string
         * @return query string
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

        protected void _setCookie(String name, Cookie cookie) {
            cookies.put(name, cookie);
        }

        private String domain;

        /**
         * Returns the domain of the request
         * @return domain
         */
        public String domain() {
            if (null == domain) resolveHostPort();
            return domain;
        }

        /**
         * Returns the port
         * @return port
         */
        public int port() {
            if (-1 == port) resolveHostPort();
            return port;
        }

        /**
         * Returns remote ip address
         * @return remote ip
         */
        protected abstract String _ip();

        private static boolean ipOk(String s) {
            return S.notEmpty(s) && S.neq("unknown", s);
        }

        private void resolveIp() {
            String rmt = _ip();
            if (!HttpConfig.isXForwardedAllowed(rmt)) {
                ip = rmt;
                return;
            }
            String s = header(X_FORWARDED_FOR);
            if (!ipOk(s)) {
                if (HttpConfig.allowExtensiveRemoteAddrResolving()) {
                    s = header(PROXY_CLIENT_IP);
                    if (!ipOk(s)) {
                        s = header(WL_PROXY_CLIENT_IP);
                        if (!ipOk(s)) {
                            s = header(HTTP_CLIENT_IP);
                            if (!ipOk(s)) {
                                s = header(HTTP_X_FORWARDED_FOR);
                                if (!ipOk(s)) {
                                    ip = rmt;
                                    return;
                                }
                            }
                        }
                    }
                } else {
                    ip = rmt;
                    return;
                }
            }

            // in case there are multiple ip addresses (due to cascade proxies), then use the first one.
            if (s.length() > 15) {
                int pos = s.indexOf(",");
                if (pos > 0) {
                    s = s.substring(0, pos);
                }
            }
            ip = s;
        }

        private void resolveHostPort() {
            String host = header(X_FORWARDED_HOST);
            if (S.empty(host)) {
                host = header(HOST);
            }
            if (null != host) {
                FastStr fs = FastStr.unsafeOf(host);
                if (fs.contains(':')) {
                    domain = fs.beforeFirst(':').toString();
                    try {
                        port = Integer.parseInt(fs.afterFirst(':').toString());
                    } catch (NumberFormatException e) {
                        port = defPort();
                    }
                } else {
                    domain = host;
                    port = defPort();
                }
            } else {
                domain = "";
                port = defPort();
            }
        }

        private int defPort() {
            return secure() ? 443 : 80;
        }

        public String ip() {
            if (null == ip) {
                resolveIp();
            }
            return ip;
        }

        public String userAgentStr() {
            return header(USER_AGENT);
        }

        public UserAgent userAgent() {
            return UserAgent.parse(userAgentStr());
        }

        protected abstract void _initCookieMap();

        /**
         * Returns cookie by it's name
         *
         * @param name the cookie name
         * @return the cookie or {@code null} if not found
         */
        public H.Cookie cookie(String name) {
            if (cookies.isEmpty()) {
                _initCookieMap();
            }
            return cookies.get(name);
        }

        /**
         * Returns all cookies of the request in Iterable
         * @return cookies
         */
        public List<H.Cookie> cookies() {
            if (cookies.isEmpty()) {
                _initCookieMap();
            }
            return C.list(cookies.values());
        }


        /**
         * resolve the request accept
         *
         * @return this request instance
         */
        private T resolveAcceptFormat() {
            String accept = header(ACCEPT);
            this.accept = Format.resolve(accept);
            return (T) this;
        }

        /**
         * Check if the requested resource is modified with etag and
         * last timestamp (usually the timestamp of a static file e.g.)
         *
         * @param etag  the etag to compare with "If_None_Match"
         *              header in browser
         * @param since the last timestamp to compare with
         *              "If_Modified_Since" header in browser
         * @return {@code true} if the resource has changed
         * or {@code false} otherwise
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
                contentType = Format.HTML;
                encoding = "utf-8";
            } else {
                String[] contentTypeParts = type.split(";");
                String _contentType = contentTypeParts[0].trim().toLowerCase();
                String _encoding = "utf-8";
                // check for encoding-info
                if (contentTypeParts.length >= 2) {
                    String[] encodingInfoParts = contentTypeParts[1].split(("="));
                    if (encodingInfoParts.length == 2 && encodingInfoParts[0].trim().equalsIgnoreCase("charset")) {
                        // encoding-info was found in request
                        _encoding = encodingInfoParts[1].trim();

                        if (S.notBlank(_encoding) &&
                                ((_encoding.startsWith("\"") && _encoding.endsWith("\""))
                                        || (_encoding.startsWith("'") && _encoding.endsWith("'")))
                                ) {
                            _encoding = _encoding.substring(1, _encoding.length() - 1).trim();
                        }
                    }
                }
                contentType = Format.resolve(_contentType);
                encoding = _encoding;
            }
        }

        /**
         * Return content type of the request
         * @return content type
         */
        public Format contentType() {
            if (null == contentType) {
                parseContentTypeAndEncoding();
            }
            return contentType;
        }

        private String encoding;

        /**
         * Returns encoding of the request
         * @return character encoding
         */
        public String characterEncoding() {
            if (null == encoding) {
                parseContentTypeAndEncoding();
            }
            return encoding;
        }

        private C.List<Locale> locales;

        private void parseLocales() {
            String s = header(ACCEPT_LANGUAGE);
            if (S.blank(s)) {
                locales = C.list(HttpConfig.defaultLocale());
                return;
            }

            // preprocess to remove all blanks
            s = S.str(s).remove(new $.F1<Character, Boolean>() {
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
                String[] arr = s0.trim().split(";");

                //Parse the locale
                Locale locale;
                String[] l = arr[0].split("-");
                switch(l.length){
                    case 2: locale = new Locale(l[0], l[1]); break;
                    case 3: locale = new Locale(l[0], l[1], l[2]); break;
                    default: locale = new Locale(l[0]); break;
                }
                lb.add(locale);
            }
            if (lb.isEmpty()) lb.add(HttpConfig.defaultLocale());

            locales = lb.toList();
        }

        /**
         * Returns locale of the request
         * @return locale
         */
        public Locale locale() {
            if (null == locales) parseLocales();
            return locales.get(0);
        }

        /**
         * Returns all locales of the request
         * @return all locales of the request
         */
        public C.List<Locale> locales() {
            if (null == locales) parseLocales();
            return locales;
        }

        private long len = -2;

        /**
         * Returns the content length of the request
         * @return content length
         */
        public long contentLength() {
            if (len > -2) return len;
            String s = header(CONTENT_LENGTH);
            if (S.blank(s)) {
                len = -1;
            } else {
                try {
                    len = Long.parseLong(s);
                } catch (NumberFormatException e) {
                    len = -1;
                    logger.error("Error parsing content-length: %s", s);
                }
            }
            return len;
        }

        public boolean readerCreated() {
            return state == State.READER;
        }

        protected abstract InputStream createInputStream();

        /**
         * Returns body of the request as binary data using {@link java.io.InputStream}
         *
         * @return input stream of the request body
         * @throws IllegalStateException if {@link #reader()} has already
         *                               been called on this request instance
         */
        public InputStream inputStream() throws IllegalStateException {
            return state.inputStream(this);
        }

        private void createReader() {
            if (null != reader) {
                return;
            }
            synchronized (this) {
                if (null != reader) {
                    return;
                }
                createInputStream();
                String charset = characterEncoding();
                Charset cs = null == charset ? Charsets.UTF_8 : Charset.forName(charset);
                reader = new InputStreamReader(inputStream(), cs);
            }
        }

        /**
         * Returns body of the request as binary data using {@link java.io.Reader}
         *
         * @return the reader of the request body
         * @throws IllegalStateException if {@link #inputStream()} has already
         *                               been called on this request instance
         */
        public Reader reader() throws IllegalStateException {
            return state.reader(this);
        }

        /**
         * Return a request parameter value by name. If there is no parameter
         * found with the name specified, then {@code null} is returned. If
         * there are multiple values associated with the name, then the
         * first one is returned
         *
         * @param name the parameter name
         * @return the parameter value of {@code null} if not found
         */
        public abstract String paramVal(String name);

        /**
         * Returns all values associated with the name specified in the
         * http request. If there is no parameter found with the name,
         * then {@code new String[0]} shall be returned
         *
         * @param name the parameter name
         * @return all values of the parameter
         */
        public abstract String[] paramVals(String name);

        /**
         * Return all parameter names
         *
         * @return an {@link java.lang.Iterable} of parameter names
         */
        public abstract Iterable<String> paramNames();

        private void parseAuthorization() {
            if (null != user) return;

            user = "";
            password = "";
            String s = header(AUTHORIZATION);
            if (null != s && s.startsWith("Basic")) {
                String data = s.substring(6);
                String[] decodedData = new String(Codec.decodeBASE64(data)).split(":");
                user = decodedData.length > 0 ? decodedData[0] : null;
                password = decodedData.length > 1 ? decodedData[1] : null;
            }
        }

        private String user;

        /**
         * The Http Basic user
         * @return the user
         */
        public String user() {
            if (null == user) parseAuthorization();
            return user;
        }

        private String password;

        /**
         * the Http Basic password
         * @return the password
         */
        public String password() {
            if (null == password) parseAuthorization();
            return password;
        }

        protected final T me() {
            return (T) this;
        }

        /**
         * Return a request instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         *
         * @param <T> the requestion type
         * @return the current request instance
         */
        @SuppressWarnings("unchecked")
        public static <T extends Request> T current() {
            return (T) Current.request();
        }

        /**
         * Set a request instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         *
         * @param <T> the response type
         * @param request the request to be set to current execution context
         */
        public static <T extends Request> void current(T request) {
            Current.request(request);
        }

        private enum State {
            NONE,
            STREAM() {
                @Override
                Reader reader(Request req) {
                    throw new IllegalStateException("reader() already called");
                }
            },
            READER() {
                @Override
                InputStream inputStream(Request req) {
                    throw new IllegalStateException("inputStream() already called");
                }
            };

            InputStream inputStream(Request req) {
                req.inputStream = req.createInputStream();
                req.state = STREAM;
                return req.inputStream;
            }

            Reader reader(Request req) {
                req.createReader();
                req.state = READER;
                return req.reader;
            }
        }

    } // eof Request

    /**
     * Defines the HTTP response trait
     */
    public static abstract class Response<T extends Response> {

        private State state = State.NONE;
        protected volatile OutputStream outputStream;
        protected volatile Writer writer;
        private Object context;


        /**
         * Attach a context object to the response instance
         * @param context the context object
         * @return the response instance itself
         */
        public T context(Object context) {
            this.context = $.notNull(context);
            return me();
        }

        /**
         * Get the context object from the response instance
         * @param <CONTEXT> the generic type of the context object
         * @return the context object
         */
        public <CONTEXT> CONTEXT context() {
            return (CONTEXT) context;
        }
        /**
         * Returns the class of the implementation. Not to be used
         * by application
         * @return implementation class
         */
        protected abstract Class<T> _impl();

        public boolean writerCreated() {
            return state == State.WRITER;
        }

        protected abstract OutputStream createOutputStream();

        private void createWriter() {
            if (null != writer) {
                return;
            }
            synchronized (this) {
                if (null != writer) {
                    return;
                }
                outputStream = createOutputStream();
                String charset = characterEncoding();
                Charset cs = null == charset ? Charsets.UTF_8 : Charset.forName(charset);
                writer = new OutputStreamWriter(outputStream, cs);
            }
        }

        /**
         * Returns the output stream to write to the response
         *
         * @return output stream to the response
         * @throws java.lang.IllegalStateException          if
         *                                                  {@link #writer()} is called already
         * @throws org.osgl.exception.UnexpectedIOException if
         *                                                  there are output exception
         */
        public OutputStream outputStream()
                throws IllegalStateException, UnexpectedIOException {
            return state.outputStream(this);
        }

        /**
         * Returns the writer to write to the response
         *
         * @return writer to the response
         * @throws java.lang.IllegalStateException          if {@link #outputStream()} is called already
         * @throws org.osgl.exception.UnexpectedIOException if there are output exception
         */
        public Writer writer()
                throws IllegalStateException, UnexpectedIOException {
            return state.writer(this);
        }

        /**
         * Returns a print writer to write to the response
         *
         * @return the print writer for the response
         * @throws IllegalStateException if {@link #outputStream()} is called already
         * @throws UnexpectedIOException if there are output exception
         */
        public PrintWriter printWriter() {
            Writer w = writer();
            if (w instanceof PrintWriter) {
                return (PrintWriter) w;
            } else {
                return new PrintWriter(w);
            }
        }

        /**
         * Returns the name of the character encoding (MIME charset)
         * used for the body sent in this response.
         * The character encoding may have been specified explicitly
         * using the {@link #characterEncoding(String)} or
         * {@link #contentType(String)} methods, or implicitly using the
         * {@link #locale(java.util.Locale)} method. Explicit specifications take
         * precedence over implicit specifications. Calls made
         * to these methods after <code>getWriter</code> has been
         * called or after the response has been committed have no
         * effect on the character encoding. If no character encoding
         * has been specified, <code>ISO-8859-1</code> is returned.
         * <p>See RFC 2047 (http://www.ietf.org/rfc/rfc2047.txt)
         * for more information about character encoding and MIME.
         *
         * @return a <code>String</code> specifying the
         * name of the character encoding, for
         * example, <code>UTF-8</code>
         */
        public abstract String characterEncoding();

        /**
         * Returns the content type used for the MIME body
         * sent in this response. The content type proper must
         * have been specified using {@link #contentType(String)}
         * before the response is committed. If no content type
         * has been specified, this method returns null.
         * If a content type has been specified, and a
         * character encoding has been explicitly or implicitly
         * specified as described in {@link #characterEncoding()}
         * or {@link #writer()} has been called,
         * the charset parameter is included in the string returned.
         * If no character encoding has been specified, the
         * charset parameter is omitted.
         *
         * @param encoding the encoding
         * @return a <code>String</code> specifying the
         * content type, for example,
         * <code>text/html; charset=UTF-8</code>,
         * or null
         */
        public abstract T characterEncoding(String encoding);

        /**
         * Set the length of the content to be write to the response
         *
         * @param len an long value specifying the length of the
         *            content being returned to the client; sets
         *            the Content-Length header
         * @return the response it self
         * @see #outputStream
         * @see #writer
         */
        public abstract T contentLength(long len);

        /**
         * Sub class to overwrite this method to set content type to
         * the response
         *
         * @param type a <code>String</code> specifying the MIME
         *             type of the content
         */
        protected abstract void _setContentType(String type);

        private String contentType;
        /**
         * Sets the content type of the response being sent to
         * the client. The content type may include the type of character
         * encoding used, for example, <code>text/html; charset=ISO-8859-4</code>.
         * If content type has already been set to the response, this method
         * will update the content type with the new value
         * 
         * <p>this method must be called before calling {@link #writer()}
         * or {@link #outputStream()}</p>
         *
         * @param type a <code>String</code> specifying the MIME
         *             type of the content
         * @return the response it self
         * @see #outputStream
         * @see #writer
         * @see #initContentType(String)
         */
        public T contentType(String type) {
            _setContentType(type);
            contentType = type;
            return me();
        }

        /**
         * This method set the content type to the response if there
         * is no content type been set already.
         *
         * @param type a <code>String</code> specifying the MIME
         *             type of the content
         * @return the response it self
         * @see #contentType(String)
         */
        public T initContentType(String type) {
            return (null == contentType) ? contentType(type) : (T) this;
        }

        public T contentDisposition(String filename, boolean inline) {
            final String type = inline ? "inline" : "attachment";
            if (S.blank(filename)) {
                header(CONTENT_DISPOSITION, type);
            } else {
                if(canAsciiEncode(filename)) {
                    String contentDisposition = "%s; filename=\"%s\"";
                    header(CONTENT_DISPOSITION, S.fmt(contentDisposition, type, filename));
                } else {
                    final String encoding = characterEncoding();
                    String contentDisposition = "%1$s; filename*="+encoding+"''%2$s; filename=\"%2$s\"";
                    try {
                        header(CONTENT_DISPOSITION, S.fmt(contentDisposition, type, URLEncoder.encode(filename, encoding)));
                    } catch (UnsupportedEncodingException e) {
                        throw E.encodingException(e);
                    }
                }
            }
            return me();
        }

        /**
         * This method will prepare response header for file download.
         * @param filename the filename
         * @return this response instance
         */
        public T prepareDownload(String filename) {
            return contentDisposition(filename, false);
        }

        /**
         * Set the etag header
         * @param etag the etag content
         * @return this response
         */
        public T etag(String etag) {
            header(ETAG, etag);
            return me();
        }

        /**
         * Sets the locale of the response, setting the headers (including the
         * Content-Type's charset) as appropriate.  This method should be called
         * before a call to {@link #writer()}.  By default, the response locale
         * is the default locale for the server.
         *
         * @param loc the locale of the response
         * @see #locale()
         */
        protected abstract void _setLocale(Locale loc);

        public T locale(Locale locale) {
            _setLocale(locale);
            return me();
        }


        /**
         * Returns the locale assigned to the response.
         *
         * @return locale
         * @see #locale(java.util.Locale)
         */
        public abstract Locale locale();

        /**
         * Adds the specified cookie to the response.  This method can be called
         * multiple times to add more than one cookie.
         *
         * @param cookie the Cookie to return to the client
         */
        public abstract void addCookie(H.Cookie cookie);


        /**
         * Returns a boolean indicating whether the named response header
         * has already been set.
         *
         * @param name the header name
         * @return <code>true</code> if the named response header
         * has already been set;
         * <code>false</code> otherwise
         */
        public abstract boolean containsHeader(String name);

        /**
         * Sends an error response to the client using the specified
         * status.  The server defaults to creating the
         * response to look like an HTML-formatted server error page
         * containing the specified message, setting the content type
         * to "text/html", leaving cookies and other headers unmodified.
         *
         * If an error-page declaration has been made for the web application
         * corresponding to the status code passed in, it will be served back in
         * preference to the suggested msg parameter.
         *
         * <p>If the response has already been committed, this method throws
         * an IllegalStateException.
         * After using this method, the response should be considered
         * to be committed and should not be written to.
         *
         * @param sc  the error status code
         * @param msg the descriptive message
         * @return the response itself
         * @throws org.osgl.exception.UnexpectedIOException If an input or output exception occurs
         * @throws IllegalStateException                    If the response was committed
         */
        public abstract T sendError(int sc, String msg);

        /**
         * Sames as {@link #sendError(int, String)} but accept message format
         * arguments
         *
         * @param sc   the error status code
         * @param msg  the descriptive message template
         * @param args the descriptive message arguments
         * @return the response itself
         * @throws org.osgl.exception.UnexpectedIOException If an input or output exception occurs
         * @throws IllegalStateException                    If the response was committed
         */
        public T sendError(int sc, String msg, Object... args) {
            return sendError(sc, S.fmt(msg, args));
        }

        /**
         * Sends an error response to the client using the specified status
         * code and clearing the buffer.
         * <p>If the response has already been committed, this method throws
         * an IllegalStateException.
         * After using this method, the response should be considered
         * to be committed and should not be written to.
         *
         * @param sc the error status code
         * @return the response itself
         * @throws org.osgl.exception.UnexpectedIOException If the response was committed before this method call
         */

        public abstract T sendError(int sc);

        /**
         * Sends a temporary redirect response to the client using the
         * specified redirect location URL.  This method can accept relative URLs;
         * the servlet container must convert the relative URL to an absolute URL
         * before sending the response to the client. If the location is relative
         * without a leading '/' the container interprets it as relative to
         * the current request URI. If the location is relative with a leading
         * '/' the container interprets it as relative to the servlet container root.
         *
         * <p>If the response has already been committed, this method throws
         * an IllegalStateException.
         * After using this method, the response should be considered
         * to be committed and should not be written to.
         *
         * @param location the redirect location URL
         * @return the response itself
         * @throws org.osgl.exception.UnexpectedIOException If the response was committed before this method call
         * @throws IllegalStateException                    If the response was committed or
         *                                                  if a partial URL is given and cannot be converted into a valid URL
         */
        public abstract T sendRedirect(String location);

        /**
         * Sets a response header with the given name and value.
         * If the header had already been set, the new value overwrites the
         * previous one.  The <code>containsHeader</code> method can be
         * used to test for the presence of a header before setting its
         * value.
         *
         * @param name  the name of the header
         * @param value the header value  If it contains octet string,
         *              it should be encoded according to RFC 2047
         *              (http://www.ietf.org/rfc/rfc2047.txt)
         * @return the response itself
         * @see #containsHeader
         * @see #addHeader
         */
        public abstract T header(String name, String value);

        /**
         * Sets the status code for this response.  This method is used to
         * set the return status code when there is no error (for example,
         * for the status codes SC_OK or SC_MOVED_TEMPORARILY).  If there
         * is an error, and the caller wishes to invoke an error-page defined
         * in the web application, the <code>sendError</code> method should be used
         * instead.
         * <p> The container clears the buffer and sets the Location header, preserving
         * cookies and other headers.
         *
         * @param sc the status code
         * @return the response itself
         * @see #sendError
         * @see #status(int)
         */
        public abstract T status(int sc);

        /**
         * Sets the status for this response.  This method is used to
         * set the return status code when there is no error (for example,
         * for the status OK or MOVED_TEMPORARILY).  If there
         * is an error, and the caller wishes to invoke an error-page defined
         * in the web application, the <code>sendError</code> method should be used
         * instead.
         * <p> The container clears the buffer and sets the Location header, preserving
         * cookies and other headers.
         *
         * @param s the status
         * @return the response itself
         * @see #sendError
         */
        public T status(Status s) {
            status(s.code());
            return me();
        }


        /**
         * Adds a response header with the given name and value.
         * This method allows response headers to have multiple values.
         *
         * @param name  the name of the header
         * @param value the additional header value   If it contains
         *              octet string, it should be encoded
         *              according to RFC 2047
         *              (http://www.ietf.org/rfc/rfc2047.txt)
         * @return this response itself
         * @see #header(String, String)
         */
        public abstract T addHeader(String name, String value);

        /**
         * Adds a response header with given name and value if the header
         * with the same name has not been added yet
         * @param name the name of the header
         * @param value the header value
         * @return this response itself
         * @see #addHeader(String, String)
         */
        public T addHeaderIfNotAdded(String name, String value) {
            if (!containsHeader(name)) {
                addHeader(name, value);
            }
            return me();
        }

        public T writeBinary(ISObject binary) {
            IO.copy(binary.asInputStream(), outputStream(), false);
            return me();
        }

        /**
         * Write a string to the response
         *
         * @param s the string to write to the response
         * @return this response itself
         */
        public T writeContent(String s) {
            try {
                IO.write(s.getBytes(characterEncoding()), outputStream());
            } catch (UnsupportedEncodingException e) {
                throw E.encodingException(e);
            }
            return me();
        }

        public abstract T writeContent(ByteBuffer buffer);

        /**
         * Write content to the response
         *
         * @param content the content to write
         * @return the response itself
         */
        public T writeText(String content) {
            _setContentType(Format.TXT.contentType());
            return writeContent(content);
        }

        /**
         * Write content to the response
         *
         * @param content the content to write
         * @return the response itself
         */
        public T writeHtml(String content) {
            _setContentType(Format.HTML.contentType());
            return writeContent(content);
        }

        /**
         * Write content to the response
         *
         * @param content the content to write
         * @return the response itself
         */
        public T writeJSON(String content) {
            _setContentType(Format.JSON.contentType());
            return writeContent(content);
        }

        /**
         * Calling this method commits the response, meaning the status
         * code and headers will be written to the client
         */
        public abstract void commit();

        /**
         * Return a request instance of the current execution context,
         * For example from a {@link java.lang.ThreadLocal}
         *
         * @param <T> the response type
         * @return the current request instance
         */
        @SuppressWarnings("unchecked")
        public static <T extends Response> T current() {
            return (T) Current.response();
        }

        /**
         * Set a request instance into the current execution context,
         * for example into a {@link java.lang.ThreadLocal}
         *
         * @param response the request to be set to current execution context
         * @param <T> the sub type of response
         */
        public static <T extends Response> void current(T response) {
            Current.response(response);
        }

        protected T me() {
            return (T) this;
        }

        private static boolean canAsciiEncode(String string) {
            CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
            return asciiEncoder.canEncode(string);
        }


        private enum State {
            NONE,
            STREAM() {
                @Override
                Writer writer(Response resp) {
                    throw new IllegalStateException("writer() already called");
                }
            },
            WRITER() {
                @Override
                OutputStream outputStream(Response resp) {
                    throw new IllegalStateException("outputStream() already called");
                }
            };

            OutputStream outputStream(Response resp) {
                resp.outputStream = resp.createOutputStream();
                resp.state = STREAM;
                return resp.outputStream;
            }

            Writer writer(Response resp) {
                resp.createWriter();
                resp.state = WRITER;
                return resp.writer;
            }
        }

    } // eof Response

    H() {
    }

    /**
     * Clear all current context
     */
    public static void cleanUp() {
        Current.clear();
    }

}
