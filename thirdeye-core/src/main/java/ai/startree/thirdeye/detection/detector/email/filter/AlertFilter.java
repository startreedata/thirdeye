/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.detector.email.filter;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.List;
import java.util.Map;

public interface AlertFilter {

  List<String> getPropertyNames();

  void setParameters(Map<String, String> props);

  boolean isQualified(MergedAnomalyResultDTO anomaly);
}
