# OSGL HTTP Change Log

1.11.0
* update osgl-tool to 1.21.0
* Support YAML Format #31

1.10.0
* update to osgl-tool-1.19.2
* H.Request - add headerNames() method that returns all header names presented in the request #30

1.9.0 30/Oct/2018
* update to osgl-tool-1.18.0
* Content type `*/*` or blank shall be resolved to `H.Format.UNKNOWN` #29

1.8.0 14/Jun/2018
* update osgl-tool to 1.15.1

1.7.1
* Allow H.Response implementation to overwrite the create `Writer` logic #28

1.7.0
* update osgl-tool to 1.13.1
* update osgl-storage to 1.6.0
* update osgl-cache to 1.4.0
* Replace FastStr with String in Path utility #27

1.6.1 13/May/2018
* update osgl-tool to 1.12.0

1.6.0
* `H.Response.writeText()` shall set `Content-Type` to `text/plaintext` #26
* Add `incr()` and `decr()` method to `H.Session` #25
* Add `H.Cookie.addToResponse()` method #24
* Update `H.Cookie` default value #23
* Add `incr()` and `decr()` method in `H.Cookie` #20
* Add `CurrentStateStorage` interface to replace previous `Current` #21
* By default allow `HttpConfig.xForwardedAllowed` #22
* update osgl-tool to 1.11.1

1.5.2 - 02/Apr/2018
* `H.Request.resolveAcceptFormat()` cannot resolve format for image types #19
* update osgl-tool to 1.9.0

1.5.1 - 25/Mar/2018
* udpate osgl-tool to 1.8.1

1.5.0 - 25/Mar/2018
* update to osgl-tool-1.8.0
* `H.Response` changes in state management of creating outputstream, writer and output #18

1.4.0 4/Mar/2018
* Update to osgl-tool-1.7.0
* `H.Response` - add `output()` method #17

1.3.0
* Allow get status from `H.Response` #15
* Minor performance improvement
* Add `H.KV.entrySet()` method
* update osgl-tool dependency to 1.6.1

1.2.3 - 15/Jan/2018
* Minor performance improvement

1.2.2 - 15/Jan/2018
* Minor performance improvement

1.2.1 - 5/Jan/2018
* Add `Content-Security-Policy` to `H.Header` #14
* Apply versioning from osgl-bootstrap #12

1.2.0 - 19/Dec/2017
* Update to osgl-1.5.0

1.1.4
* `H.Request.accept` parsing shall support `text/css` #13

1.1.3
- Deprecate H.Status.UNAVAILABLE_FOR_LEGAL_REASON to H.Status.UNAVAILABLE_FOR_LEGAL_REASONS

1.1.2
- Support unknown HTTP method #7

1.1.1
- Define http status int constant #4

1.0.6
- `H.Response.writeBinary` shall close the outputstream #5

1.0.5
- `H.Method.actionMethod()` shall include `H.Method.Patch` #3

1.0.4
- `H.Session` and `H.Flash` shall implement `hashCode()` and `equals()` method #2

1.0.3
- Decouple the osgl-storage dependency by making it provided scope

1.0.2
- Take out version range. See https://issues.apache.org/jira/browse/MNG-3092

1.0.1
- H.Rquest.fullUrl() shall not output `80` when sending to port `80` #1

1.0.0
- baseline from 0.5

0.5.0-SNAPSHOT
- update tool to 0.10.0
- update storage to 0.8.0
- update cache to 0.5.0
- add H.MediaType enum

0.4.0-SNAPSHOT
- add H.Response.writeBinary(ISObject) API

0.3.4-SNAPSHOT
- update HttpConfig cache service relevant API

0.3.3-SNAPSHOT
- update osgl-storage to 0.7.3-SNAPSHOT

0.3.1-SNAPSHOT
- Add secure configuration to HttpConfig
- Add H.Response.prepareDownload(String, String) method
- Add H.Format.isText() method
- Add H.Request.referer() and H.Request.referrer() methods

0.3.0-SNAPSHOT
- update to osgl-tool 0.9
- allow parsing remote address aggressively
- Add H.Format.CSS and H.Format.JAVASCRIPT content type

0.2.5-SNAPSHOT
- update osgl-tool to 0.7.1-SNAPSHOT

0.2.4-SNAPSHOT
- H.Session.getId() should try to pull from data map before generating new one
- H.Request.format() now renamed to H.Request.accept()
- H.Request.contentType() now return H.Format type
- H.Status now implemented with class instead of enum. And it allows to create non-standard http status instance
- H.Format now implemented with class instead of enum. And it allows to create custom http format instances
- H.Cookie, H.Session, H.Status, H.Format now implement Serializable interface

0.2.3-SNAPSHOT
- Fix issue in getting expires() on H.Cookie when maxAge is less than zero

0.2.2-SNAPSHOT
- Remove threadlocal from cookie operations on Request
- Add accept(Format) method to H.Request to allow set a accept to request

0.2.1-SNAPSHOT
- add FOUND_AJAX(278) as new http status
- KV as abstract class for Session and Flash

0.2-SNAPSHOT
- upgrade to osgl-tool 0.4-SNAPSHOT
- H.Request param now support multiple param values
- Binding now take H.Request instead of Map<String, String[]> as parameter source
- Add more methods to H.Response and H.Cookie

0.1-SNAPSHOT
- base version when history log started
