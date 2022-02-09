/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.spi.rootcause.Entity;
import ai.startree.thirdeye.spi.rootcause.MaxScoreSet;
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import ai.startree.thirdeye.spi.rootcause.impl.DimensionEntity;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import java.util.Map;
import java.util.Set;

/**
 * The MetricDimensionPipeline extracts filters of input metrics as DimensionEntities.
 */
public class MetricDimensionPipeline extends Pipeline {

  /**
   * Constructor for dependency injection
   *
   * @param outputName pipeline output name
   * @param inputNames input pipeline names
   */
  public MetricDimensionPipeline(String outputName, Set<String> inputNames) {
    super();
  }

  /**
   * Alternate constructor for RCAFrameworkLoader
   *
   * @param outputName pipeline output name
   * @param inputNames input pipeline names
   * @param ignore configuration properties (ignore)
   */
  public MetricDimensionPipeline(String outputName, Set<String> inputNames,
      Map<String, Object> ignore) {
    super();
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<MetricEntity> metrics = context.filter(MetricEntity.class);
    Set<Entity> output = new MaxScoreSet<>();

    for (MetricEntity metric : metrics) {
      for (Map.Entry<String, String> entry : metric.getFilters().entries()) {
        output.add(DimensionEntity
            .fromDimension(metric.getScore(), entry.getKey(), entry.getValue(),
                DimensionEntity.TYPE_GENERATED));
      }
    }

    return new PipelineResult(context, output);
  }
}
