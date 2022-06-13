/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public class AnomalyFeedbackIndex extends AbstractIndexEntity {

  String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
