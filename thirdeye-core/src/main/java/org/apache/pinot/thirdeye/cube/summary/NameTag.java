package org.apache.pinot.thirdeye.cube.summary;

import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.cube.data.dbrow.DimensionValues;

public class NameTag {

  public static final String ALL = "(ALL)";
  public static final String NOT_ALL = "(ALL)-";
  public static final String EMPTY = "";

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

  void setNotAll(int index) {
    names.set(index, NOT_ALL);
  }

  void setEmpty(int index) {
    names.set(index, EMPTY);
  }
}
