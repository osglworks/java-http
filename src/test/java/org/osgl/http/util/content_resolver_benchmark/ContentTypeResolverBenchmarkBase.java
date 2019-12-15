package org.osgl.http.util.content_resolver_benchmark;

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

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.junit.Test;
import org.osgl.http.BenchmarkBase;
import org.osgl.http.H;
import org.osgl.http.util.ContentTypeResolver;

@BenchmarkOptions(warmupRounds = 100 * 100 * 50, benchmarkRounds = 100 * 100 * 100 * 5)
public abstract class ContentTypeResolverBenchmarkBase extends BenchmarkBase {

    private static H.Format DEF = H.Format.UNKNOWN;

    private String target;

    public ContentTypeResolverBenchmarkBase(String target) {
        this.target = target;
    }

    @Test
    public void runNew() {
        ContentTypeResolver.resolve(DEF, target);
    }

    @Test
    public void runLegacy() {
        ContentTypeResolver.resolveLegacy(target);
    }

}
