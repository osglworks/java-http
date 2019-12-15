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

import org.osgl.http.H;
import org.osgl.util.S;

/**
 * Provide fast content type string parsing for:
 * - Content-Type
 * - Accept
 *
 * Note:
 *
 * 1. This is not fully implementation of [Content Negotiation](https://tools.ietf.org/html/rfc2296)
 * 2. This is **not** an accurate validator of content-type or accept string
 * 3. The aim is to do a really fast triage on detecting MIME type based on the fact that 99% of case user agent sent
 *    out the correct string and we only aim to detect the commonly used types:
 *    - text/html
 *    - text/css
 *    - text/csv
 *    - text/plain
 *    - text/yaml
 *    - text/x-yaml
 *    - text/vnd.yaml
 *    - text/xml
 *    - text/javascript
 *    - application/json
 *    - application/javascript
 *    - application/xml
 *    - application/pdf
 *    - application/yaml
 *    - application/x-yaml
 *    - application/vnd.yaml
 *    - application/vnd.ms-word
 *    - application/vnd.ms-excel
 *    - application/vnd.openxmlformats-officedocument.wordprocessingml.document
 *    - application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
 *    - image/png
 *    - image/gif
 *    - image/jpeg
 *    - image/svg+xml
 *    - image/bmp
 *    - image/ico
 *    - video/mp4
 *    - video/mpeg
 *    - video/mov
 *    - video/ogg
 *    - video/x-flv
 *    For all other mimetype it returns UNKNOWN directly
 *
 * Refer:
 * - [List of default Accept values](https://developer.mozilla.org/en-US/docs/Web/HTTP/Content_negotiation/List_of_default_Accept_values)
 */
public class ContentTypeResolver {

    // shortcut
    private static final int HC_JSON = "application/json".hashCode();

    // first level traits
    private static final char[] TXT = {'t', 'e', 'x', 't'};
    private static final char[] IMG = {'i', 'm', 'a', 'g', 'e'};
    private static final char[] APP = {'a', 'p', 'p', 'l', 'i', 'c', 'a', 't', 'i', 'o', 'n'};
    private static final char[] MULTIPART = {'m', 'u', 'l', 't', 'i', 'p', 'a', 'r', 't'};
    private static final char[] AUDIO = {'a', 'u', 'd', 'i', 'o'};
    private static final char[] VIDEO = {'v', 'i', 'd', 'e', 'o'};

    // second level traits
    private static final char[] HTML = {'h', 't', 'm', 'l'};
    private static final char[] PLAIN = {'p', 'l', 'a', 'i', 'n'};
    private static final char[] CSS = {'c', 's', 's'};
    private static final char[] CSV = {'c', 's', 'v'};
    private static final char[] JS = {'j', 'a', 'v', 'a', 's', 'c', 'r', 'i', 'p', 't'};
    private static final char[] JSON = {'j', 's', 'o', 'n'};
    private static final char[] XHTML = {'x', 'h', 't', 'm', 'l', '+', 'x', 'm', 'l'};
    private static final char[] FORM_URL_ENCODED = {'x', '-', 'w', 'w', 'w', '-', 'f', 'o', 'r', 'm', '-', 'u', 'r', 'l', 'e', 'n', 'c', 'o', 'd', 'e', 'd'};
    private static final char[] XML = {'x', 'm', 'l'};
    private static final char[] PDF = {'p', 'd', 'f'};
    private static final char[] YAML = {'y', 'a', 'm', 'l'};
    private static final char[] V_YAML = {'v', 'n', 'd', '.', 'y', 'a', 'm', 'l'};
    private static final char[] X_YAML = {'x', '-', 'y', 'a', 'm', 'l'};

    // images
    private static final char[] BMP = {'b', 'm', 'p'};
    private static final char[] GIF = {'g', 'i', 'f'};
    private static final char[] ICO = {'i', 'c', 'o'};
    private static final char[] JPEG = {'j', 'p', 'e', 'g'};
    private static final char[] PNG = {'p', 'n', 'g'};
    private static final char[] SVG = {'s', 'v', 'g', '+', 'x', 'm', 'l'};

    // audio
    private static final char[] MPA = {'m', 'p', 'e', 'g'};
    private static final char[] MP3 = {'m', 'p', '3'};
    private static final char[] MOD = {'m', 'o', 'd'};
    private static final char[] WAV = {'w', 'a', 'v'};
    private static final char[] OGA = {'o', 'g', 'a'};

    // videos
    private static final char[] MPEG = {'m', 'p', 'e', 'g'};
    private static final char[] MP4 = {'m', 'p', '4'};
    private static final char[] MOV = {'m', 'o', 'v'};
    private static final char[] WEBM = {'w', 'e', 'b', 'm'};
    private static final char[] FLV = {'x', '-', 'f', 'l', 'v'};
    private static final char[] OGG = {'o', 'g', 'g'};

    // ms-app - for IE
    private static final char[] MSA = {'x', '-', 'm', 's', '-', 'a', 'p', 'p', 'l', 'i', 'c', 'a', 't', 'i', 'o', 'n'};

    public static H.Format resolve(String target) {
        return resolve(H.Format.UNKNOWN, target);
    }

    public static H.Format resolve(H.Format def, String target) {
        if (null == def) {
            def = H.Format.UNKNOWN;
        }
        if (null == target) {
            return def;
        }
        int targetLen = target.length();
        if (targetLen < 3) {
            return def;
        }
        char[] trait;
        char c = target.charAt(0);
        switch (c) {
            case 't':
                trait = TXT;
                break;
            case 'a':
                c = target.charAt(1);
                if ('p' == c) {
                    trait = APP;
                } else if ('u' == c) {
                    trait = AUDIO;
                } else {
                    return def;
                }
                break;
            case 'i':
                trait = IMG;
                break;
            case 'm':
                trait = MULTIPART;
                break;
            case 'v':
                trait = VIDEO;
                break;
            default:
                return def;
        }

        int traitLen = trait.length;
        if (targetLen < traitLen + 4) {
            // the part after "/" should be at least three chars long
            return def;
        }
        c = target.charAt(traitLen);
        if ('/' != c) {
            return def;
        }
        int start = traitLen + 1;
        if (trait == TXT) {
            return parseText(def, target, targetLen, start);
        } else if (APP == trait) {
            return parseApplication(def, target, targetLen, start);
        } else if (IMG == trait) {
            return parseImage(H.Format.PNG, target, targetLen, start);
        } else if (MULTIPART == trait) {
            return parseMultipart(def, target, targetLen, start);
        } else if (VIDEO == trait) {
            return parseVideo(H.Format.MP4, target, targetLen, start);
        } else {
            return parseAudio(H.Format.WAV, target, targetLen, start);
        }
    }

    private static boolean isSeparator(char c) {
        return ',' == c || ';' == c;
    }

    private static H.Format verifySecondLevelTrait(H.Format def, String target, int targetLen, int start, char[] trait, H.Format candidate) {
        int len = trait.length + start;
        if (len == targetLen) {
            return candidate;
        } else if (targetLen < len) {
            return def;
        }
        char c = target.charAt(len);
        return isSeparator(c) ? candidate : def;
    }

    private static H.Format parseMultipart(H.Format def, String target, int targetLen, int start) {
        // let's assume it is always form-multipart
        return H.Format.FORM_MULTIPART_DATA;
    }

    /**
     * quick parse for
     * - html
     * - css
     * - csv
     * - plain
     */
    private static H.Format parseText(H.Format def, String target, int targetLen, int start) {
        char[] trait;
        H.Format candidate;
        switch (target.charAt(start)) {
            case 'h':
                trait = HTML;
                candidate = H.Format.HTML;
                break;
            case 'c':
                char c = target.charAt(start + 1);
                if ('s' == c) {
                    switch (target.charAt(start + 2)) {
                        case 'v':
                            trait = CSV;
                            candidate = H.Format.CSV;
                            break;
                        case 's':
                            trait = CSS;
                            candidate = H.Format.CSS;
                            break;
                        default:
                            return def;
                    }
                } else {
                    return def;
                }
                break;
            case 'p':
                trait = PLAIN;
                candidate = H.Format.TXT;
                break;
            case 'x':
                c = target.charAt(start + 1);
                if ('m' == c) {
                    trait = XML;
                    candidate = H.Format.XML;
                } else if ('-' == c) {
                    trait = X_YAML;
                    candidate = H.Format.YAML;
                } else {
                    return def;
                }
                break;
            case 'y':
                trait = YAML;
                candidate = H.Format.YAML;
                break;
            case 'j':
                trait = JS;
                candidate = H.Format.JAVASCRIPT;
                break;
            case 'v':
                trait = V_YAML;
                candidate = H.Format.YAML;
                break;
            default:
                return def;
        }

        return verifySecondLevelTrait(def, target, targetLen, start, trait, candidate);
    }

    /**
     * Quick parse for
     * - json
     * - javascript
     */
    private static H.Format parseApplication(H.Format def, String target, int targetLen, int start) {
        char[] trait;
        H.Format candidate;
        char c = target.charAt(start);
        switch (c) {
            case 'j':
                c = target.charAt(start + 1);
                if ('s' == c) {
                    trait = JSON;
                    candidate = H.Format.JSON;
                } else if ('a' == c) {
                    trait = JS;
                    candidate = H.Format.JAVASCRIPT;
                } else {
                    return def;
                }
                break;
            case 'x':
                c = target.charAt(start + 1);
                if ('m' == c) {
                    trait = XML;
                    candidate = H.Format.XML;
                } else if ('h' == c) {
                    trait = XHTML;
                    candidate = H.Format.HTML;
                } else if ('-' == c) {
                    c = target.charAt(start + 2);
                    if ('w' == c) {
                        trait = FORM_URL_ENCODED;
                        candidate = H.Format.FORM_URL_ENCODED;
                    } else if ('y' == c) {
                        trait = X_YAML;
                        candidate = H.Format.YAML;
                    } else if ('m' == c) {
                        trait = MSA;
                        candidate = H.Format.HTML;
                    } else {
                        return def;
                    }
                } else {
                    return def;
                }
                break;
            case 'p':
                trait = PDF;
                candidate = H.Format.PDF;
                break;
            case 'y':
                trait = YAML;
                candidate = H.Format.YAML;
                break;
            case 'v':
                if (targetLen > start + 4) {
                    if ('.' == target.charAt(start + 3)) {
                        return parseVnd(def, target, targetLen, start + 4);
                    }
                }
                return def;
            default:
                return def;
        }
        return verifySecondLevelTrait(def, target, targetLen, start, trait, candidate);
    }

    private static H.Format parseImage(H.Format def, String target, int targetLen, int start) {
        char[] trait;
        H.Format candidate;
        char c = target.charAt(start);
        switch (c) {
            case 'p':
                trait = PNG;
                candidate = H.Format.PNG;
                break;
            case 'j':
                trait = JPEG;
                candidate = H.Format.JPG;
                break;
            case 'g':
                trait = GIF;
                candidate = H.Format.GIF;
                break;
            case 's':
                trait = SVG;
                candidate = H.Format.SVG;
                break;
            case 'b':
                trait = BMP;
                candidate = H.Format.BMP;
                break;
            case 'i':
                trait = ICO;
                candidate = H.Format.ICO;
                break;
            default:
                return def;
        }
        return verifySecondLevelTrait(def, target, targetLen, start, trait, candidate);
    }

    private static H.Format parseAudio(H.Format def, String target, int targetLen, int start) {
        char[] trait;
        H.Format candidate;
        char c = target.charAt(start);
        switch (c) {
            case 'm':
                c = target.charAt(start + 1);
                if ('o' == c) {
                    trait = MOD;
                    candidate = H.Format.MOD;
                    break;
                } else if ('p' == c) {
                    if (targetLen < 12) {
                        if ('3' == target.charAt(targetLen - 1)) {
                            return H.Format.MP3;
                        } else {
                            return H.Format.MPA;
                        }
                    } else {
                        if ('3' == target.charAt(start + 2) || '3' == target.charAt(start + 4)) {
                            // matches mp3 and mpeg3
                            return H.Format.MP3;
                        } else {
                            return H.Format.MPA;
                        }
                    }
                } else {
                    return def;
                }
            case 'o':
                trait = OGA;
                candidate = H.Format.OGA;
                break;
            case 'w':
                trait = WAV;
                candidate = H.Format.WAV;
                break;
            default:
                return def;
        }
        return verifySecondLevelTrait(def, target, targetLen, start, trait, candidate);
    }

    private static H.Format parseVideo(H.Format def, String target, int targetLen, int start) {
        char[] trait;
        H.Format candidate;
        char c = target.charAt(start);
        switch (c) {
            case 'm':
                c = target.charAt(start + 1);
                if ('o' == c) {
                    trait = MOV;
                    candidate = H.Format.MOV;
                    break;
                } else if ('p' == c) {
                    c = target.charAt(start + 2);
                    if ('4' == c) {
                        trait = MP4;
                        candidate = H.Format.MP4;
                    } else if ('e' == c) {
                        trait = MPEG;
                        candidate = H.Format.MPG;
                    } else {
                        return def;
                    }
                    break;
                } else {
                    return def;
                }
            case 'o':
                trait = OGG;
                candidate = H.Format.OGV;
                break;
            case 'w':
                trait = WEBM;
                candidate = H.Format.WEBM;
                break;
            case 'x':
                trait = FLV;
                candidate = H.Format.FLV;
                break;
            default:
                return def;
        }
        return verifySecondLevelTrait(def, target, targetLen, start, trait, candidate);
    }

    private static H.Format parseVnd(H.Format def, String target, int targetLen, int start) {
        if ('y' == target.charAt(start)) {
            if (targetLen == start + 4) {
                if (target.regionMatches(start + 1, "yaml", 1, 3)) {
                    return H.Format.YAML;
                }
            }
            return def;
        }
        if ('m' == target.charAt(start)) {
            if (targetLen == start + "ms-excel".length()) {
                if (target.regionMatches(start + 1, "ms-excel", 1, "ms-excel".length() - 1)) {
                    return H.Format.XLS;
                }
                return def;
            }
            if (targetLen == start + "ms-word".length()) {
                if (target.regionMatches(start + 1, "ms-word", 1, "ms-word".length() - 1)) {
                    return H.Format.DOC;
                }
                return def;
            }
            return def;
        }
        if (target.indexOf("sheet", start) > -1) {
            return H.Format.XLSX;
        } else if (target.indexOf("word", start) > -1) {
            return H.Format.DOCX;
        } else {
            return def;
        }
    }

    public static H.Format resolveLegacy(String contentType) {
        H.Format fmt = H.Format.UNKNOWN;
        if (S.blank(contentType) || "*/*".equals(contentType)) {
            fmt = H.Format.UNKNOWN;
        } else if (contentType.startsWith("application/json")) {
            fmt = H.Format.JSON;
        } else if (contentType.startsWith("text/html")) {
            fmt = H.Format.HTML;
        } else if (contentType.startsWith("text/css")) {
            fmt = H.Format.CSS;
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            fmt = H.Format.FORM_URL_ENCODED;
        } else if (contentType.startsWith("multipart/form-data") || contentType.startsWith("multipart/mixed")) {
            fmt = H.Format.FORM_MULTIPART_DATA;
        } else if (contentType.startsWith("text/javascript")) {
            fmt = H.Format.JSON;
        } else if (contentType.startsWith("application/xml") || contentType.contains("text/xml")) {
            fmt = H.Format.XML;
        } else if (contentType.startsWith("image")) {
            if (contentType.contains("png")) {
                fmt = H.Format.PNG;
            } else if (contentType.contains("jpg") || contentType.contains("jpeg")) {
                fmt = H.Format.JPG;
            } else if (contentType.contains("gif")) {
                fmt = H.Format.GIF;
            } else if (contentType.contains("svg")) {
                fmt = H.Format.SVG;
            } else if (contentType.contains("ico")) {
                fmt = H.Format.ICO;
            } else if (contentType.contains("bmp")) {
                fmt = H.Format.BMP;
            } else {
                // just specify an arbitrary sub type
                // see https://superuser.com/questions/979135/is-there-a-generic-mime-type-for-all-image-files
                fmt = H.Format.PNG;
            }
        } else if (contentType.contains("text/plain")) {
            fmt = H.Format.TXT;
        } else if (contentType.contains("csv") || contentType.contains("comma-separated-values")) {
            fmt = H.Format.CSV;
        } else if (contentType.contains("ms-excel")) {
            fmt = H.Format.XLS;
        } else if (contentType.contains("spreadsheetml")) {
            fmt = H.Format.XLSX;
        } else if (contentType.contains("pdf")) {
            fmt = H.Format.PDF;
        } else if (contentType.contains("msword")) {
            fmt = H.Format.DOC;
        } else if (contentType.contains("wordprocessingml")) {
            fmt = H.Format.DOCX;
        } else if (contentType.contains("rtf")) {
            fmt = H.Format.RTF;
        } else if (contentType.contains("yaml")) {
            fmt = H.Format.YAML;
        } else if (contentType.contains("audio")) {
            if (contentType.contains("mpeg3")) {
                fmt = H.Format.MP3;
            } else if (contentType.contains("mp")) {
                fmt = H.Format.MPA;
            } else if (contentType.contains("mod")) {
                fmt = H.Format.MOD;
            } else if (contentType.contains("wav")) {
                fmt = H.Format.WAV;
            } else if (contentType.contains("ogg")) {
                fmt = H.Format.OGA;
            } else {
                // just specify an arbitrary sub type
                // see https://superuser.com/questions/979135/is-there-a-generic-mime-type-for-all-image-files
                fmt = H.Format.WAV;
            }
        } else if (contentType.contains("video")) {
            if (contentType.contains("mp4")) {
                fmt = H.Format.MP4;
            } else if (contentType.contains("webm")) {
                fmt = H.Format.WEBM;
            } else if (contentType.contains("ogg")) {
                fmt = H.Format.OGV;
            } else if (contentType.contains("mov")) {
                fmt = H.Format.MOV;
            } else if (contentType.contains("mpeg")) {
                fmt = H.Format.MPG;
            } else if (contentType.contains("x-flv")) {
                fmt = H.Format.FLV;
            } else {
                // just specify an arbitrary sub type
                // see https://superuser.com/questions/979135/is-there-a-generic-mime-type-for-all-image-files
                fmt = H.Format.MP4;
            }
        }

        return fmt;
    }

    public static void main(String[] args) {
        String s = "application/x-ms-application";
        for (char c : s.toCharArray()) {
            System.out.print(", '" + c + "'");
        }
    }
}
