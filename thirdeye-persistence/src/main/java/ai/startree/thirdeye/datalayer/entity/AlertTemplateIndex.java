/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public class AlertTemplateIndex extends AbstractIndexEntity {

  private String name;

  public String getName() {
    return name;
  }

  public AlertTemplateIndex setName(final String name) {
    this.name = name;
    return this;
  }
}
