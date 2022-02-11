/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.detector.email.filter;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DummyAlertFilter extends BaseAlertFilter {

  @Override
  public List<String> getPropertyNames() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public void setParameters(Map<String, String> props) {
    // Does nothing
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    return true;
  }

  @Override
  public String toString() {
    return "DummyFilter";
  }
}
