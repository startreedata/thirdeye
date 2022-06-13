/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public class GenericJsonEntity extends AbstractEntity {

  protected String jsonVal;

  protected String type;

  public String getJsonVal() {
    return jsonVal;
  }

  public void setJsonVal(String jsonVal) {
    this.jsonVal = jsonVal;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
