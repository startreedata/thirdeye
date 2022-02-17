/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public class DetectionAlertConfigIndex extends AbstractIndexEntity {

  String name;
  String application;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }
}


