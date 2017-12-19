package org.osgl.http;

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
import org.osgl.util.C;

import java.io.*;
import java.util.List;
import java.util.Set;

public class FormatTest extends TestBase {
    @Test
    public void predefinedFormatOrdinalShallBeDifferentFromEachOther() {
        Set<Integer> ordinals = C.newSet();
        List<H.Format> predefined = H.Format.predefined();
        for (H.Format f : predefined) {
            ordinals.add(f.ordinal());
        }
        eq(ordinals.size(), predefined.size());
    }

    @Test
    public void deserializePredefinedOrdinalShallBeManaged() throws IOException, ClassNotFoundException {
        H.Format f1 = H.Format.HTML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(f1);
        byte[] ba = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object f2 = ois.readObject();
        same(f1, f2);
    }

    @Test
    public void deserializeCustomFormatOrdinalShallNotBeManaged() throws IOException, ClassNotFoundException {
        H.Format f1 = H.Format.of("some", "abc/xyz");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(f1);
        byte[] ba = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object f2 = ois.readObject();
        assertNotSame(f1, f2);
        eq(f1, f2);
    }
}
