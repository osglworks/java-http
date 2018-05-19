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

import org.junit.Test;
import org.osgl.http.TestBase;
import org.osgl.util.Unsafe;

import java.util.Iterator;
import java.util.List;

public class PathTest extends TestBase {

    @Test
    public void tokenizeStart0EndWithTerminator() {
        String url = "/foo/service/389724df43274ew89qrew/name?tk=32498739";
        List<String> cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }

    @Test
    public void tokenizeStart0EndWithTerminatorNoLeadingSeparator() {
        String url = "foo/service/389724df43274ew89qrew/name?tk=32498739";
        List<String> cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }

    @Test
    public void tokenizeStartNEndWithTerminator() {
        String url = "http://abc.com/foo/service/389724df43274ew89qrew/name?tk=32498739";
        List<String> cl = Path.tokenize(Unsafe.bufOf(url), "http://abc.com".length());
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }

    @Test
    public void tokenizeMergeSeparators() {
        String url = "/foo/service//389724df43274ew89qrew///name//";
        List<String> cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }

    @Test
    public void tokenizeStart0EndWithSeparator() {
        String url = "/foo/service/389724df43274ew89qrew/name/";
        List<String> cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }

    @Test
    public void tokenizeStart0EndWithNothing() {
        String url = "/foo/service/389724df43274ew89qrew/name";
        List<String> cl = Path.tokenize(Unsafe.bufOf(url));
        ceq("foo", cl.get(0));
        ceq("service", cl.get(1));
        ceq("389724df43274ew89qrew", cl.get(2));
        ceq("name", cl.get(3));
        same(4, cl.size());
    }

    @Test
    public void tokenizerStart0EndWithTerminator() {
        String url = "/foo/service/389724df43274ew89qrew/name?tk=32498739";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStart0EndWithTerminatorWithNoLeadingSeparator() {
        String url = "foo/service/389724df43274ew89qrew/name?tk=32498739";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStart0EndWithSeparator() {
        String url = "/foo/service/389724df43274ew89qrew/name/";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStartNEndWithSeparator() {
        String url = "http://abc.com/foo/service/389724df43274ew89qrew/name/";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url), "http://abc.com/".length());
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStartNEndWithTerminatorWithNoLeadingSeparator() {
        String url = "foo/service/389724df43274ew89qrew/name/";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStart0EndWithNothing() {
        String url = "/foo/service/389724df43274ew89qrew/name";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStart0MergeSeparator() {
        String url = "/foo/service//389724df43274ew89qrew////name///";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

    @Test
    public void tokenizerStart0MergeSeparatorEndWithTerminator() {
        String url = "/foo/service//389724df43274ew89qrew////name///?dsdf=32";
        Iterator<String> itr = Path.tokenizer(Unsafe.bufOf(url));
        ceq("foo", itr.next());
        ceq("service", itr.next());
        ceq("389724df43274ew89qrew", itr.next());
        ceq("name", itr.next());
        no(itr.hasNext());
    }

}
