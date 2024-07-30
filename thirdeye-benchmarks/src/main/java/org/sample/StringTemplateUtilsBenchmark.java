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

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.util.StringTemplateUtils;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
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
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class StringTemplateUtilsBenchmark {

  AlertTemplateDTO template;
  Map<String, Object> valuesMap;

  @Setup
  public void setup() throws IOException {
    String alertTemplateDtoString = IOUtils.resourceToString("/alertTemplateDto.json",
        StandardCharsets.UTF_8);
    template = Constants.TEMPLATABLE_OBJECT_MAPPER.readValue(alertTemplateDtoString,
        AlertTemplateDTO.class);
    valuesMap = ImmutableMap.<String, Object>builder()
        .put("aggregationColumn", "views")
        .put("completenessDelay", "P0D")
        .put("monitoringGranularity", "P1D")
        .put("max", "${max}")
        .put("timezone", "UTC")
        .put("queryFilters", "")
        .put("aggregationFunction", "sum")
        .put("rcaExcludedDimensions", List.of())
        .put("timeColumnFormat", "1,DAYS,SIMPLE_DATE_FORMAT,yyyyMMdd")
        .put("timeColumn", "date")
        .put("min", "${min}")
        .put("rcaAggregationFunction", "")
        .put("queryLimit", "100000000")
        .put("startTime", 1)
        .put("endTime", 2)
        .put("dataSource", "pinotQuickStartLocal")
        .put("dataset", "pageviews")
        .build();
  }

  @Benchmark
  public void applyContext(Blackhole blackhole) throws IOException {
    blackhole.consume(StringTemplateUtils.applyContext(template, valuesMap));
  }
}
