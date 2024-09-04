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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.sample.SRN2.Resource;

@Fork(value = 1, jvmArgsPrepend = "-Xmx128m")
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Srn2Benchmark {

  @Benchmark
  public void runCorrectImplem(Blackhole blackhole) throws IOException {
    blackhole.consume(correctImplem(TargetType.EXAMPLE1, "1", "database1"));
  }

  @Benchmark
  public void runPinotImplem(Blackhole blackhole) throws IOException {
    blackhole.consume(pinotImpl(TargetType.EXAMPLE1, "1", "database1"));
  }


  public static SRN2 correctImplem(TargetType targetType, String targetId, String database) {
    final List<Resource> resourceHierarchy = new ArrayList<>();
    resourceHierarchy.add(new Resource("CLUSTER_LEVEL", "_clusterName"));
    resourceHierarchy.add(new Resource("WORKSPACE_LEVEL", database));
    return new SRN2(resourceHierarchy);
  }


  public static SRN2 pinotImpl(TargetType targetType, String targetId, String database) {
    StringBuilder sb = new StringBuilder(org.sample.SRN2.SRN2_PREFIX).append(org.sample.SRN2.SRN2_DELIMITER).append("CLUSTER_LEVEL")
        .append(org.sample.SRN2.SRN2_RESOURCE_TYPE_NAME_DELIMITER).append("_clusterName");

    if (true) {
      database = Optional.ofNullable(database).orElse("DEFAULT_DATABASE");
      sb.append(org.sample.SRN2.SRN2_DELIMITER).append("WORKSPACE_LEVEL").append(org.sample.SRN2.SRN2_RESOURCE_TYPE_NAME_DELIMITER)
          .append(database);
    }

    // Add the table name if targetType is table.
    if (false) {
      sb.append(org.sample.SRN2.SRN2_DELIMITER).append("TABLE_LEVEL").append(org.sample.SRN2.SRN2_RESOURCE_TYPE_NAME_DELIMITER)
          .append(targetId);
    }

    return org.sample.SRN2.fromString(sb.toString());
  }
}
