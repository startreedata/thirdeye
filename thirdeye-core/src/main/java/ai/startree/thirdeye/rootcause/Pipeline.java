/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import java.util.Set;

/**
 * Interface for a named stateless pipeline as injected into {@code RCAFramework}. Holds the
 * business logic for
 * associating search context entities with other relevant entities. Also performs relative ranking
 * of associated entities in terms of importance to the user.
 *
 * @see RCAFramework
 */
@Deprecated
public abstract class Pipeline {

  private String outputName;
  private Set<String> inputNames;

  public void init(PipelineInitContext context) {
    outputName = context.getOutputName();
    inputNames = context.getInputNames();
  }

  public final String getOutputName() {
    return outputName;
  }

  public final Set<String> getInputNames() {
    return inputNames;
  }

  /**
   * Executes the pipeline given the execution context set up by the RCAFramework. Returns entities
   * as determined relevant given the user-specified search context (contained in the execution
   * context).
   *
   * @param context pipeline execution context
   * @return pipeline results
   */
  public abstract PipelineResult run(PipelineContext context);
}
