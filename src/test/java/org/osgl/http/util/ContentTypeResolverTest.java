package org.osgl.http.util;

/*-
 * #%L
 * OSGL HTTP
 * %%
 * Copyright (C) 2017 - 2019 OSGL (Open Source General Library)
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

import org.junit.Test;
import org.osgl.http.H;
import org.osgl.http.TestBase;

import static org.osgl.http.H.Format.*;

public class ContentTypeResolverTest extends TestBase {
    @Test
    public void testTextParsing() {
        t(HTML, "text/html");
        t(HTML, "text/html;q=0.3");
        t(HTML, "text/html; charset=UTF-8");
        t(HTML, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        t(TXT, "text/plain");
        t(TXT, "text/plain;q=0.1");
        t(TXT, "text/plain,application/xml");
        t(CSS, "text/css");
        t(CSS, "text/css;q=0.1");
        t(CSS, "text/css,application/xml");
        t(CSV, "text/csv");
        t(CSV, "text/csv;q=0.1");
        t(CSV, "text/csv,application/xml");
        t(JAVASCRIPT, "text/javascript");
        t(YAML, "text/yaml");
        t(YAML, "text/x-yaml");
        t(YAML, "text/vnd.yaml");
    }

    @Test
    public void testApplicationParsing() {
        t(JSON, "application/json");
        t(JSON, "application/json;q=0.3");
        t(JSON, "application/json,application/xml;q=0.1");

        t(JAVASCRIPT, "application/javascript");
        t(JAVASCRIPT, "application/javascript;q=0.3");
        t(JAVASCRIPT, "application/javascript,application/xml;q=0.1");

        t(FORM_URL_ENCODED, "application/x-www-form-urlencoded");
        t(FORM_MULTIPART_DATA, "multipart/form-data");

        t(HTML, "application/x-ms-application");

        t(XML, "application/xml");

        t(PDF, "application/pdf");

        t(YAML, "application/yaml");
        t(YAML, "application/x-yaml");
        t(YAML, "application/vnd.yaml");

        t(XLS, "application/vnd.ms-excel");
        t(XLSX, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        t(DOC, "application/vnd.ms-word");
        t(DOCX, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    public void testImageParsing() {
        t(JPG, "image/jpeg");
        t(JPG, "image/jpeg;q=0.3");
        t(JPG, "image/jpeg,image/svg+xml");

        t(GIF, "image/gif");
        t(GIF, "image/gif;q=0.3");
        t(GIF, "image/gif,image/svg+xml");

        t(PNG, "image/png");
        t(PNG, "image/png;q=0.3");
        t(PNG, "image/png,image/svg+xml");

        t(SVG, "image/svg+xml");
        t(SVG, "image/svg+xml;q=0.3");
        t(SVG, "image/svg+xml,image/svg+xml");

        t(BMP, "image/bmp");
        t(BMP, "image/bmp;q=0.3");
        t(BMP, "image/bmp,image/svg+xml");

        t(ICO, "image/ico");
        t(ICO, "image/ico;q=0.3");
        t(ICO, "image/ico,image/svg+xml");
    }

    @Test
    public void testAudioParsing() {
        t(MOD, "audio/mod");
        t(MOD, "audio/mod;q=0.3");
        t(MOD, "audio/mod,audio/svg+xml");

        t(MPA, "audio/mpeg");
        t(MPA, "audio/mpeg;q=0.3");
        t(MPA, "audio/mpeg,audio/svg+xml");

        t(MP3, "audio/mpeg3");
        t(MP3, "audio/mpeg3;q=0.3");
        t(MP3, "audio/mpeg3,audio/svg+xml");

        t(MP3, "audio/mp3");
        t(MP3, "audio/mp3;q=0.3");
        t(MP3, "audio/mp3,audio/svg+xml");

        t(WAV, "audio/wav");
        t(WAV, "audio/wav;q=0.3");
        t(WAV, "audio/wav,audio/svg+xml");

        t(WAV, "audio/x-unknown");
        t(WAV, "audio/x-unknown;q=0.3");
        t(WAV, "audio/x-unknown,audio/svg+xml");

        t(OGA, "audio/oga");
        t(OGA, "audio/oga;q=0.3");
        t(OGA, "audio/oga,audio/svg+xml");

        t(OGA, "audio/ogg");
        t(OGA, "audio/ogg;q=0.3");
        t(OGA, "audio/ogg,audio/svg+xml");
    }
    
    @Test
    public void testVideoParsing() {
        t(MOV, "video/mov");
        t(MOV, "video/mov;q=0.3");
        t(MOV, "video/mov,video/svg+xml");

        t(MPG, "video/mpeg");
        t(MPG, "video/mpeg;q=0.3");
        t(MPG, "video/mpeg,video/svg+xml");

        t(MP4, "video/mp4");
        t(MP4, "video/mp4;q=0.3");
        t(MP4, "video/mp4,video/svg+xml");

        t(WEBM, "video/webm");
        t(WEBM, "video/webm;q=0.3");
        t(WEBM, "video/webm,video/svg+xml");

        t(FLV, "video/x-flv");
        t(FLV, "video/x-flv;q=0.3");
        t(FLV, "video/x-flv,video/svg+xml");

        t(OGV, "video/ogg");
        t(OGV, "video/ogg;q=0.3");
        t(OGV, "video/ogg,video/svg+xml");
    }

    private void t(H.Format expected, String s) {
        eq(expected, ContentTypeResolver.resolve(s));
    }
}
