/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.v2;

/**
 * Defines an input for any PlanNode, with plan node name and all the keys of outputs.
 */
public class PlanNodeInput {
  private String inputKey;
  private String nodeName;
  private String outputKey;

  public String getInputKey() {
    return inputKey;
  }

  public PlanNodeInput setInputKey(final String inputKey) {
    this.inputKey = inputKey;
    return this;
  }

  public String getNodeName() {
    return nodeName;
  }

  public PlanNodeInput setNodeName(final String nodeName) {
    this.nodeName = nodeName;
    return this;
  }

  public String getOutputKey() {
    return outputKey;
  }

  public PlanNodeInput setOutputKey(final String outputKey) {
    this.outputKey = outputKey;
    return this;
  }
}
