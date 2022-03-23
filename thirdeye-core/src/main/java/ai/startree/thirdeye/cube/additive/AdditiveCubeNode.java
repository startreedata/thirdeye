/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.additive;

import ai.startree.thirdeye.cube.data.node.BaseCubeNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A CubeNode for additive metrics such as page view count.
 */
public class AdditiveCubeNode extends BaseCubeNode<AdditiveCubeNode, AdditiveRow> {

  private double baselineValue;
  private double currentValue;

  /**
   * Constructs a root CubeNode whose level and index is 0 and parent pointer is null.
   *
   * @param data the data of this root node.
   */
  public AdditiveCubeNode(AdditiveRow data) {
    super(data);
    this.baselineValue = data.getBaselineValue();
    this.currentValue = data.getCurrentValue();
  }

  /**
   * Constructs a CubeNode which is specified information.
   *
   * @param level the level of this node.
   * @param index the index of this node that is located in its parent's children list.
   * @param data the data of this node.
   * @param parent the parent of this node.
   */
  public AdditiveCubeNode(int level, int index, AdditiveRow data, AdditiveCubeNode parent) {
    super(level, index, data, parent);
    this.baselineValue = data.getBaselineValue();
    this.currentValue = data.getCurrentValue();
  }

  @Override
  public void resetValues() {
    this.baselineValue = this.data.getBaselineValue();
    this.currentValue = this.data.getCurrentValue();
  }

  @Override
  public void removeNodeValues(AdditiveCubeNode node) {
    this.baselineValue -= node.baselineValue;
    this.currentValue -= node.currentValue;
  }

  @Override
  public void addNodeValues(AdditiveCubeNode node) {
    this.baselineValue += node.baselineValue;
    this.currentValue += node.currentValue;
  }

  @Override
  public double getBaselineSize() {
    return baselineValue;
  }

  @Override
  public double getCurrentSize() {
    return currentValue;
  }

  @Override
  public double getOriginalBaselineSize() {
    return data.getBaselineValue();
  }

  @Override
  public double getOriginalCurrentSize() {
    return data.getCurrentValue();
  }

  @Override
  public double getBaselineValue() {
    return baselineValue;
  }

  @Override
  public double getCurrentValue() {
    return currentValue;
  }

  @Override
  @JsonIgnore
  public double getOriginalBaselineValue() {
    return data.getBaselineValue();
  }

  @Override
  @JsonIgnore
  public double getOriginalCurrentValue() {
    return data.getCurrentValue();
  }

  @Override
  public double originalChangeRatio() {
    return data.currentValue / data.baselineValue;
  }

  @Override
  public double changeRatio() {
    return currentValue / baselineValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AdditiveCubeNode)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AdditiveCubeNode that = (AdditiveCubeNode) o;
    return Double.compare(that.baselineValue, baselineValue) == 0
        && Double.compare(that.currentValue, currentValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), baselineValue, currentValue);
  }

  /**
   * ToString that handles if the given cube node is null, i.e., a root cube node. Moreover, it does
   * not invoke
   * parent's toString() to prevent multiple calls of toString to their parents.
   *
   * @param node the node to be converted to string.
   * @return a simple string representation of a parent cube node, which does not toString its
   *     parent node recursively.
   */
  private String toStringAsParent(AdditiveCubeNode node) {
    if (node == null) {
      return "null";
    } else {
      return MoreObjects.toStringHelper(this)
          .add("level", level)
          .add("index", index)
          .add("baselineValue", baselineValue)
          .add("currentValue", currentValue)
          .add("cost", cost)
          .add("data", data)
          .toString();
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("level", level)
        .add("index", index)
        .add("baselineValue", baselineValue)
        .add("currentValue", currentValue)
        .add("cost", cost)
        .add("data", data)
        .add("parent", toStringAsParent(parent))
        .toString();
  }
}
