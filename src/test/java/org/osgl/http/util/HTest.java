package org.osgl.http.util;

import org.junit.Before;
import org.junit.Test;
import org.osgl.http.HttpConfig;
import org.osgl.http.TestBase;

public class HTest extends TestBase {

    @Before
    public void setup() {
        HttpConfig.domain("osgl.org");
    }

    @Test
    public void fullUrlShallNotOutputStandardPort() {
        HttpConfig.secure(false);
        HttpConfig.nonSecurePort(80);
        eq("http://osgl.org/foo", Path.fullUrl("foo"));
    }

    @Test
    public void fullUrlShallOutputNonStandardPort() {
        HttpConfig.secure(false);
        HttpConfig.nonSecurePort(8080);
        eq("http://osgl.org:8080/foo", Path.fullUrl("foo"));
    }

    @Test
    public void fullUrlShallNotOutputStandardSecurePort() {
        HttpConfig.secure(true);
        HttpConfig.securePort(443);
        eq("https://osgl.org/foo", Path.fullUrl("foo"));
    }

    @Test
    public void fullUrlShallOutputNonStandardSecurePort() {
        HttpConfig.secure(true);
        HttpConfig.securePort(8080);
        eq("https://osgl.org:8080/foo", Path.fullUrl("foo"));
    }

}
