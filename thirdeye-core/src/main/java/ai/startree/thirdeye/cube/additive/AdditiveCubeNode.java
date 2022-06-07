/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.additive;

import ai.startree.thirdeye.cube.data.cube.CubeUtils;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A CubeNode for additive metrics such as page view count.
 */
public class AdditiveCubeNode {

  private int level;
  private int index;
  private double cost;
  private AdditiveRow data;
  private AdditiveCubeNode parent;
  private List<AdditiveCubeNode> children = new ArrayList<>();
  private double baselineValue;
  private double currentValue;

  /**
   * Constructs a root CubeNode whose level and index is 0 and parent pointer is null.
   *
   * @param data the data of this root node.
   */
  public AdditiveCubeNode(AdditiveRow data) {
    this.data = Preconditions.checkNotNull(data);
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
    this(data);
    this.level = level;
    this.index = index;
    Preconditions.checkArgument((level != 0 && parent != null) || (level == 0 && parent == null));
    this.parent = parent;
    if (parent != null) { // non root node
      Dimensions parentDimension = new Dimensions(
          parent.getDimensions().namesToDepth(parent.getLevel()));
      Dimensions childDimension = new Dimensions(data.getDimensions().namesToDepth(level));
      Preconditions.checkState(parentDimension.isParentOf(childDimension),
          "Current node is not a child node of the given parent node. Current and parent dimensions: ",
          data.getDimensions(),
          parent.getDimensions());
      parent.children.add(this);
      // Sort node from large to small to increase stability of this algorithm.
      // The reason is that the parent values will dynamically be updated whenever a child is extracted. In addition,
      // large children are unlikely to be interfered by small children. Therefore, evaluating large children before
      // small children can increase the stability of this algorithm.
      parent.children.sort((Object o1, Object o2) ->
          (int) (
              (((AdditiveCubeNode) o2).getBaselineSize() + ((AdditiveCubeNode) o2).getCurrentSize())
                  - (
                  ((AdditiveCubeNode) o1).getBaselineSize()
                      + ((AdditiveCubeNode) o1).getCurrentSize()))
      );
    }
    this.baselineValue = data.getBaselineValue();
    this.currentValue = data.getCurrentValue();
  }

  /**
   * Resets all values (e.g., baseline and current value) of this node.
   */
  public void resetValues() {
    this.baselineValue = this.data.getBaselineValue();
    this.currentValue = this.data.getCurrentValue();
  }

  /**
   * Updates all values when the child node is extracted from this node.
   *
   * @param node the child node to be extracted.
   */
  public void removeNodeValues(AdditiveCubeNode node) {
    this.baselineValue -= node.baselineValue;
    this.currentValue -= node.currentValue;
  }

  /**
   * Updates all values when an extracted child node is added back to this node.
   *
   * @param node the child node to be added back.
   */
  public void addNodeValues(AdditiveCubeNode node) {
    this.baselineValue += node.baselineValue;
    this.currentValue += node.currentValue;
  }

  /**
   * Returns the latest node size of baseline time period.
   *
   * @return the latest node size of baseline time period.
   */
  public double getBaselineSize() {
    return baselineValue;
  }

  /**
   * Returns the latest node size of current time period.
   *
   * @return the latest node size of current time period.
   */
  public double getCurrentSize() {
    return currentValue;
  }

  /**
   * Returns the original node size of baseline time period.
   *
   * @return the original node size of baseline time period.
   */
  public double getOriginalBaselineSize() {
    return data.getBaselineValue();
  }

  /**
   * Returns the original node size of current time period.
   *
   * @return the original node size of current time period.
   */
  public double getOriginalCurrentSize() {
    return data.getCurrentValue();
  }

  /**
   * Returns the changeRatio that is calculated by the aggregate current and aggregate baseline
   * values of all children node.
   *
   * @return aggregated current value of all children / aggregated baseline value of all children;
   */
  public double originalChangeRatio() {
    return data.currentValue / data.baselineValue;
  }

  /**
   * Returns the changeRatio that is calculated by currentValue and baselineValue.
   *
   * @return currentValue / baselineValue;
   */
  public double changeRatio() {
    return currentValue / baselineValue;
  }

  /**
   * Returns if the data of current node equals to the data of other node. The parent and children
   * nodes are not
   * compared due to the cyclic references between parent and children nodes.
   *
   * @param o the other node.
   * @return true if the data of current node equals to the data of other node.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AdditiveCubeNode)) {
      return false;
    }
    AdditiveCubeNode that = (AdditiveCubeNode) o;
    return level == that.level && index == that.index && Double.compare(that.cost, cost) == 0
        && Objects.equal(data, that.data) && Double.compare(that.baselineValue, baselineValue) == 0
        && Double.compare(that.currentValue, currentValue) == 0;
  }

  /**
   * Returns the hash code that is generated base on the data of this node.
   *
   * @return the hash code that is generated base on the data of this node.
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(level, index, cost, data, baselineValue, currentValue);
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

  /**
   * Returns the level of this node in the cube; level 0 the topmost level.
   *
   * @return the level of this node in the cube.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Returns the latest cost of this node.
   *
   * @return the latest cost of this node.
   */
  public double getCost() {
    return cost;
  }

  /**
   * Sets the latest cost of this node.
   *
   * @param cost the latest cost of this node.
   */
  public void setCost(double cost) {
    this.cost = cost;
  }

  /**
   * Returns the dimension names of this node, e.g., ["country", "page key"]
   *
   * @return the dimension names of this node.
   */
  @JsonIgnore
  public Dimensions getDimensions() {
    return data.getDimensions();
  }

  /**
   * Returns the dimension values of this node, e.g., ["US", "linkedin.com"]
   *
   * @return the dimension values of this node.
   */
  @JsonIgnore
  public DimensionValues getDimensionValues() {
    return data.getDimensionValues();
  }

  /**
   * Returns the parent of this node.
   *
   * @return the parent of this node.
   */
  public AdditiveCubeNode getParent() {
    return parent;
  }

  /**
   * Returns the number of children of this node.
   *
   * @return the number of children of this node.
   */
  public int childrenSize() {
    return children.size();
  }

  /**
   * Returns the children list of this node.
   *
   * @return the children list of this node.
   */
  public List<AdditiveCubeNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  /**
   * Returns the change ratio of the node if it is a finite number; otherwise, provide an
   * alternative change ratio.
   *
   */
  public double safeChangeRatio() {
    double ratio = changeRatio();
    if (Double.isFinite(ratio) && Double.compare(ratio, 0d) != 0) {
      return ratio;
    } else {
      ratio = originalChangeRatio();
      if (Double.isFinite(ratio) && Double.compare(ratio, 0d) != 0) {
        return CubeUtils.ensureChangeRatioDirection(getBaselineSize(), getCurrentSize(), ratio);
      } else {
        if (parent != null) {
          return CubeUtils.ensureChangeRatioDirection(getBaselineSize(), getCurrentSize(),
              parent.safeChangeRatio());
        } else {
          return 1.;
        }
      }
    }
  }

  /**
   * Returns the current changeRatio of this node is increased or decreased, i.e., returns true if
   * changeRatio of the node >= 1.0.
   * If the current changeRatio is NAN, then the changeRatio of the aggregated values is used.
   *
   * Precondition: the aggregated baseline and current values cannot both be zero.
   */
  public boolean side() {
    double ratio = changeRatio();
    if (!Double.isNaN(ratio)) {
      return Double.compare(1., changeRatio()) <= 0;
    } else {
      return Double.compare(1., originalChangeRatio()) <= 0;
    }
  }
}
