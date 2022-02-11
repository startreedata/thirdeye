/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import java.util.HashSet;

/**
 * EmptyPipeline serves as a dummy implementation or sink that does not emit any output.
 * Can be used to construct an validate a DAG without a full implementation of component pipelines.
 */
public class EmptyPipeline extends Pipeline {

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, new HashSet<>());
  }
}
