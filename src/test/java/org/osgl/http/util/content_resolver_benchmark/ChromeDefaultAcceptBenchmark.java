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

@BenchmarkOptions(warmupRounds = 100 * 100 * 20, benchmarkRounds = 100 * 100 * 100)
public class ChromeDefaultAcceptBenchmark extends ContentTypeResolverBenchmarkBase {
    public ChromeDefaultAcceptBenchmark() {
        super("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    }
}
