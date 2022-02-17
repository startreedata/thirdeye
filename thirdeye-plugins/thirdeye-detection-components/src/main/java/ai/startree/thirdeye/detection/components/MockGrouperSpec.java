/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MockGrouperSpec extends AbstractSpec {

  private double mockParam = Double.NaN;

  public double getMockParam() {
    return mockParam;
  }

  public void setMockParam(double mockParam) {
    this.mockParam = mockParam;
  }
}

