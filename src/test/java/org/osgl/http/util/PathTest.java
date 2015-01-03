package org.osgl.http.util;

import org.junit.Test;
import org.osgl.http.TestBase;
import org.osgl.util.Unsafe;

import java.util.List;

public class PathTest extends TestBase {

    @Test
    public void testTokenizer() {
        String url = "/foo/service/389724df43274ew89qrew/name?tk=32498739";
        List<CharSequence> cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());

        url = "foo/service/389724df43274ew89qrew/name/";
        cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }
}
