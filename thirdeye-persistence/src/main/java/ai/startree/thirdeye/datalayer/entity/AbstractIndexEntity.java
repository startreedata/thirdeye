/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public abstract class AbstractIndexEntity extends AbstractEntity {

  protected Long baseId;

  public Long getBaseId() {
    return baseId;
  }

  public void setBaseId(Long baseId) {
    this.baseId = baseId;
  }
}
