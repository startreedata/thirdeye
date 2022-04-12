/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.spi.rootcause.Entity;

/**
 * NullPipeline serves as a dummy implementation or sink that emits as output any received inputs.
 * Can be used to construct an validate a DAG without a full implementation of component pipelines.
 */
public class NullPipeline extends Pipeline {

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, context.filter(Entity.class));
  }
}
