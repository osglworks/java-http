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

import org.junit.Before;
import org.junit.Test;
import org.osgl.http.H;
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

    @Test
    public void testYamlContentType() {
        eq("text/vnd.yaml", H.Format.YAML.contentType());
        eq(H.Format.YAML, H.Format.resolve("text/x-yaml"));
        eq(H.Format.YAML, H.Format.resolve("text/yaml"));
        eq(H.Format.YAML, H.Format.resolve("application/x-yaml"));
    }

    @Test
    public void testQrCodeContentType() {
        eq("image/png", H.Format.of("qrcode").contentType());
    }

}
