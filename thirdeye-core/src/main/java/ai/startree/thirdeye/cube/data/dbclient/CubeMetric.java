/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import ai.startree.thirdeye.cube.data.dbrow.Row;
import java.util.List;
import java.util.Map;

public interface CubeMetric<T extends Row> {
  String getDataset();

  String getMetric();

  List<CubeSpec> getCubeSpecs();

  void fillValueToRowTable(Map<List<String>, T> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag);
}
