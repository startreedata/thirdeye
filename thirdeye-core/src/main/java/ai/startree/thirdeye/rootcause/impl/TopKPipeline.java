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
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import ai.startree.thirdeye.spi.rootcause.util.EntityUtils;
import java.util.Map;

/**
 * TopKPipeline is a generic pipeline implementation for ordering, filtering, and truncating
 * incoming
 * Entities. The pipeline first filters incoming Entities based on their {@code class} and then
 * orders them based on score from highest to lowest. It finally truncates the result to at most
 * {@code k} elements and emits the result.
 */
public class TopKPipeline extends Pipeline {

  public static final String PROP_K = "k";
  public static final String PROP_CLASS = "class";

  public static final String PROP_CLASS_DEFAULT = Entity.class.getName();

  private int k;
  private Class<? extends Entity> clazz;

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();

    if (!properties.containsKey(PROP_K)) {
      throw new IllegalArgumentException(
          String.format("Property '%s' required, but not found", PROP_K));
    }
    this.k = Integer.parseInt(properties.get(PROP_K).toString());

    String classProp = PROP_CLASS_DEFAULT;
    if (properties.containsKey(PROP_CLASS)) {
      classProp = properties.get(PROP_CLASS).toString();
    }
    try {
      this.clazz = (Class<? extends Entity>) Class.forName(classProp);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, EntityUtils.topk(context.filter(this.clazz), this.k));
  }
}
