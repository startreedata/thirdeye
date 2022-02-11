/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.api.RootCauseEntity;
import ai.startree.thirdeye.spi.rootcause.Entity;

/**
 * Foundation class for building UI formatters for RCA Entities. Takes in RCA Entities and returns
 * RootCauseEntity container instances that contain human-readable data for display on the GUI.
 */
public abstract class RootCauseEntityFormatter {
  public abstract boolean applies(Entity entity);

  public abstract RootCauseEntity format(Entity entity);

  public static RootCauseEntity makeRootCauseEntity(Entity entity, String type, String label, String link) {
    RootCauseEntity out = new RootCauseEntity();
    out.setUrn(entity.getUrn());
    out.setScore(entity.getScore());
    out.setType(type);
    out.setLabel(label);
    out.setLink(link);
    return out;
  }
}
