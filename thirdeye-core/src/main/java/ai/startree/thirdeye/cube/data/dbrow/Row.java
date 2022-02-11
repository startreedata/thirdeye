/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbrow;

import ai.startree.thirdeye.cube.data.node.CubeNode;

public interface Row {

  /**
   * Returns the dimension names of this row, such as ["country", "page key"].
   *
   * @return the dimension names of this row.
   */
  Dimensions getDimensions();

  /**
   * Sets dimension names of this row, such as ["country", "page key"].
   *
   * @param dimensions the dimension names for this row.
   */
  void setDimensions(Dimensions dimensions);

  /**
   * Returns dimension values of this row, such as ["US", "linkedin.com"]
   *
   * @return dimension values of this row, such as ["US", "linkedin.com"]
   */
  DimensionValues getDimensionValues();

  /**
   * Sets dimension values of this row, such as ["US", "linkedin.com"]
   *
   * @param dimensionValues the dimension values for this row.
   */
  void setDimensionValues(DimensionValues dimensionValues);

  /**
   * Converts current row to a CubeNode.
   *
   * @return a CubeNode of this row.
   */
  CubeNode toNode();

  /**
   * Converts current row to a CubeNode.
   *
   * @param level the level of this node; 0 is the top level.
   * @param index the index of this node, which is used for speeding up algorithm speed.
   * @param parent the parent of this node.
   * @return a CubeNode of this row.
   */
  CubeNode toNode(int level, int index, CubeNode parent);

  @Override
  boolean equals(Object o);

  @Override
  int hashCode();
}
