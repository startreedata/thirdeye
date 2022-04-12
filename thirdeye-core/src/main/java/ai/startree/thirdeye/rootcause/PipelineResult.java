/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.spi.rootcause.Entity;
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
