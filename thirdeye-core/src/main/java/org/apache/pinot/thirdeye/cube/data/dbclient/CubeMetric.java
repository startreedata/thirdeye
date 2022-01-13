package org.apache.pinot.thirdeye.cube.data.dbclient;

import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.data.dbrow.Row;

public interface CubeMetric<T extends Row> {
  String getDataset();

  String getMetric();

  List<CubeSpec> getCubeSpecs();

  void fillValueToRowTable(Map<List<String>, T> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag);
}
