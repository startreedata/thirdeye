/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TriggerConditionGrouperSpec extends AbstractSpec {

  private String expression;
  private String operator;
  private Map<String, Object> leftOp;
  private Map<String, Object> rightOp;

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public Map<String, Object> getLeftOp() {
    return leftOp;
  }

  public void setLeftOp(Map<String, Object> leftOp) {
    this.leftOp = leftOp;
  }

  public Map<String, Object> getRightOp() {
    return rightOp;
  }

  public void setRightOp(Map<String, Object> rightOp) {
    this.rightOp = rightOp;
  }
}

