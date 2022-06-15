/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
package ai.startree.thirdeye.plugins.detection.components;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.Grouper;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A sample mock grouper to test the Grouper Interface
 */
public class MockGrouper implements Grouper<MockGrouperSpec> {

  private static final String mockDimKey = "mock_dimension_name";
  private static final String mockDimValue = "test_value";

  @Override
  public void init(MockGrouperSpec spec, InputDataFetcher dataFetcher) {
  }

  @Override
  public void init(final MockGrouperSpec spec) {

  }

  @Override
  public List<MergedAnomalyResultDTO> group(List<MergedAnomalyResultDTO> anomalies) {
    // A sample code for testing the grouper interface.
    List<MergedAnomalyResultDTO> groupedAnomalies = new ArrayList<>();
    for (MergedAnomalyResultDTO anomaly : anomalies) {
      if (anomaly != null && anomaly.getDimensions() != null
          && anomaly.getDimensions().get(mockDimKey) != null) {
        if (anomaly.getDimensions().get(mockDimKey).equals(mockDimValue)) {
          Map<String, String> properties = new HashMap<>();
          properties.put("TEST_KEY", "TEST_VALUE");
          anomaly.setProperties(properties);
          groupedAnomalies.add(anomaly);
        }
      }
    }

    return groupedAnomalies;
  }
}
