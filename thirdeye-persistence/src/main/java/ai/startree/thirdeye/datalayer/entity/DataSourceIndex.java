/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public class DataSourceIndex extends AbstractIndexEntity {

  private String name;
  private String type;

  public String getName() {
    return name;
  }

  public DataSourceIndex setName(final String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public DataSourceIndex setType(final String type) {
    this.type = type;
    return this;
  }
}
