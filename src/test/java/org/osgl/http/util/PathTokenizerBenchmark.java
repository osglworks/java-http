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

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.junit.Test;
import org.osgl.http.BenchmarkBase;
import org.osgl.util.C;
import org.osgl.util.Unsafe;

@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 10, clock = Clock.NANO_TIME, concurrency = -1)
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmark-tokenizer")
public class PathTokenizerBenchmark extends BenchmarkBase {

    private static final String URL = "/foo/service/389724df43274ew89qrew/name?tk=32498739";

    @Test
    public void pathTokenizerSplit() {
        for (int i = 0; i < 1000 * 1000; ++i) {
            Path.tokenize(Unsafe.bufOf(URL), 0, '/', '?');
        }
    }

    @Test
    public void stringSplit() {
        for (int i = 0; i < 1000 * 1000; ++i) {
            C.listOf(URL.split("\\/"));
        }
    }
}
