/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.api.RootCauseEntity;
import ai.startree.thirdeye.spi.rootcause.Entity;

/**
 * Default formatter that applies to any Entity. Provides minimal information and serves as
 * a fallback.
 */
public class DefaultEntityFormatter extends RootCauseEntityFormatter {
  public static final String TYPE_OTHER = "other";

  @Override
  public boolean applies(Entity entity) {
    return true;
  }

  @Override
  public RootCauseEntity format(Entity entity) {
    return makeRootCauseEntity(entity, TYPE_OTHER, entity.getUrn(), null);
  }
}
