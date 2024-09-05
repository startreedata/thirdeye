/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.sample;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, jvmArgsPrepend = "-Xmx128m")
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringConcatBenchmark {

  @Benchmark
  public String stringFormat() {
    return String.format("%s:%s:%s:%s:%s", "SRN_PREFIX", "ZONE", "entityType", "namespace",
        "entityName");
  }

  @Benchmark
  public String stringBuilder() {
    return new
        StringBuilder("SRN_PREFIX")
        .append(":")
        .append("ZONE")
        .append(":")
        .append("entityType")
        .append(":")
        .append("namespace")
        .append(":")
        .append("entityName").toString();
  }

  @Benchmark
  public String stringConcat() {
    return "SRN_PREFIX" + ":" + "ZONE" + ":" + "entityType" + ":" + "namespace" + ":"
        + "entityName";
  }

  @Benchmark
  public String stringJoin() {
    return String.join(":", List.of("SRN_PREFIX", "ZONE", "entityType", "namespace", "entityName"));
  }
}
