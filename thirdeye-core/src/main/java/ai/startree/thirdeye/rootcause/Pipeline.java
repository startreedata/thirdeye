/*
 * Copyright 2022 StarTree Inc
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
