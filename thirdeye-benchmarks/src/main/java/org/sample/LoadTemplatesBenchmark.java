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

import ai.startree.thirdeye.plugins.bootstrap.opencore.OpenCoreBoostrapResourcesProvider;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, jvmArgsPrepend = "-Xmx256m")
@Measurement(iterations = 7, time = 3, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 7, time = 3, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class LoadTemplatesBenchmark {

  private static final TypeReference<List<AlertTemplateApi>> ref = new TypeReference<>() {};

  @Param({"10", "100"})
  int numNamespaces;

  OpenCoreBoostrapResourcesProvider openCoreBoostrapResourcesProvider;

  @Setup
  public void setup() {
    openCoreBoostrapResourcesProvider = new OpenCoreBoostrapResourcesProvider();
  }

  @Benchmark
  public void loadWithNoCache(Blackhole blackhole) throws IOException {
    for (int i = 0; i < numNamespaces; i++) {
      blackhole.consume(openCoreBoostrapResourcesProvider.getAlertTemplates());
    }
  }

  @Benchmark
  public void loadWithCacheAndCopy(Blackhole blackhole) throws IOException {
    final List<AlertTemplateApi> alertTemplates = openCoreBoostrapResourcesProvider.getAlertTemplates();
    for (int i = 0; i < numNamespaces; i++) {
      blackhole.consume(copy((alertTemplates)));
    }
  }

  public static List<AlertTemplateApi> copy(final List<AlertTemplateApi> alertTemplates) {
    try {
      byte[] t = Constants.VANILLA_OBJECT_MAPPER.writeValueAsBytes(alertTemplates);
      return Constants.VANILLA_OBJECT_MAPPER.readValue(t, ref);
    } catch (IOException e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
  }
}
