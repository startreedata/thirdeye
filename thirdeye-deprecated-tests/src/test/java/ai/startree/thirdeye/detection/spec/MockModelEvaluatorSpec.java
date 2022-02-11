/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spec;

import ai.startree.thirdeye.detection.spi.model.ModelStatus;
import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class MockModelEvaluatorSpec extends AbstractSpec {

  private ModelStatus mockModelStatus;

  public ModelStatus getMockModelStatus() {
    return mockModelStatus;
  }

  public void setMockModelStatus(ModelStatus mockModelStatus) {
    this.mockModelStatus = mockModelStatus;
  }
}
