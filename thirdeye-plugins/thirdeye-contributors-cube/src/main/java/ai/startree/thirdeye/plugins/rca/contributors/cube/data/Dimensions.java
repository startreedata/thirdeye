/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.rca.contributors.cube.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

// fixme cyril remove this is weaker than a list + isParentof is containsAll
public class Dimensions {

  @JsonProperty("names")
  private final ImmutableList<String> names;

  public Dimensions() {
    names = ImmutableList.of();
  }

  public Dimensions(List<String> names) {
    this.names = ImmutableList.copyOf(names);
  }

  public int size() {
    return names.size();
  }

  public String get(int index) {
    return names.get(index);
  }

  /**
   * Returns all dimensions
   */
  public List<String> names() {
    return names;
  }

  /**
   * Returns a sublist of dimension names to the specified depth. Depth starts from 0, which is the
   * top level.
   *
   * @param depth the depth of the sublist.
   * @return a sublist of dimension names to the specified depth.
   */
  public List<String> namesToDepth(int depth) {
    return names.subList(0, depth);
  }

  /**
   * Checks if the current dimension is the parent to the given dimension. A dimension A is a parent
   * to dimension B if
   * and only if dimension A is a subset of dimension B.
   *
   * @param child the given child dimension.
   * @return true if the current dimension is a parent (subset) to the given dimension.
   */
  public boolean isParentOf(Dimensions child) {
    if (child
        == null) { // null dimension is always the top level and hence it is a parent to every dimension
      return false;
    }
    if (names.size() >= child.size()) {
      return false;
    }
    if (names.size() == 0) {
      return true;
    }
    Set<String> childDim = new HashSet<>(child.names);
    for (String name : names) {
      if (!childDim.contains(name)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Dimensions that = (Dimensions) o;
    return Objects.equals(names, that.names);
  }

  @Override
  public int hashCode() {
    return Objects.hash(names);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }
}
