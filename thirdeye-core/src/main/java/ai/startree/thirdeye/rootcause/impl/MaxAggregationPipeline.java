/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.MaxScoreSet;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
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
