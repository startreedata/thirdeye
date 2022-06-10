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

import java.util.HashSet;
import java.util.Set;

/**
 * Container object for pipeline execution results. Holds entities with scores as set by the
 * pipeline.
 */
public class PipelineResult {

  private final PipelineContext context;
  private final Set<Entity> entities;

  public PipelineResult(PipelineContext context, Set<? extends Entity> entities) {
    this.context = context;
    this.entities = new HashSet<>(entities);
  }

  public PipelineContext getContext() {
    return context;
  }

  public Set<Entity> getEntities() {
    return entities;
  }
}
