/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.util.EntityUtils;

/**
 * NormalizationPipeline normalizes entity scores to a [0.0,1.0] interval based on observed
 * minimum and maximum scores.
 */
public class NormalizationPipeline extends Pipeline {

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, EntityUtils.normalizeScores(context.filter(Entity.class)));
  }
}
