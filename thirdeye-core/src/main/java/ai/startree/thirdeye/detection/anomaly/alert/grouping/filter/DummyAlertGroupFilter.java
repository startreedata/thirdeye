/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping.filter;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import java.util.Map;

public class DummyAlertGroupFilter extends BaseAlertGroupFilter {

  @Override
  public void setParameters(Map<String, String> props) {
  }

  @Override
  public boolean isQualified(GroupedAnomalyResultsDTO groupedAnomaly) {
    return true;
  }
}
