/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
