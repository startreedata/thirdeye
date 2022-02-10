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
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.spi.rootcause.Entity;
import ai.startree.thirdeye.spi.rootcause.MaxScoreSet;
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import ai.startree.thirdeye.spi.rootcause.util.EntityUtils;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an aggregator that handles the same entity being returned from multiple
 * pipelines by selecting the entity with the highest score. It optionally truncates the
 * number of returned entities to the top k by score.
 */
public class MaxAggregationPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(MaxAggregationPipeline.class);

  private final static String PROP_K = "k";
  private final static int PROP_K_DEFAULT = -1;

  private int k;

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();
    this.k = MapUtils.getIntValue(properties, PROP_K, PROP_K_DEFAULT);
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context,
        EntityUtils.topk(new MaxScoreSet<>(context.filter(Entity.class)), this.k));
  }
}
