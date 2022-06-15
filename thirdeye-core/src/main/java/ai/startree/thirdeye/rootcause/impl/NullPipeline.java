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
package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;

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
