/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.summary;

import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import java.util.ArrayList;
import java.util.List;

public class NameTag {

  public static final String ALL = "(ALL)";
  public static final String ALL_OTHERS = "(ALL_OTHERS)";
  public static final String EMPTY = "(NO_FILTER)";

  public final List<String> names;

  NameTag(int levelCount) {
    names = new ArrayList<>(levelCount);
    for (int i = 0; i < levelCount; ++i) {
      names.add(ALL);
    }
  }

  public List<String> getNames() {
    return names;
  }

  void copyNames(DimensionValues dimensionValues) {
    for (int i = 0; i < dimensionValues.size(); ++i) {
      names.set(i, dimensionValues.get(i));
    }
  }

  void setAllOthers(int index) {
    names.set(index, ALL_OTHERS);
  }

  void setEmpty(int index) {
    names.set(index, EMPTY);
  }
}
