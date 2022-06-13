/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping;

import java.util.Collections;
import java.util.Map;

public abstract class BaseAlertGrouper implements AlertGrouper {

  protected Map<String, String> props = Collections.emptyMap();

  @Override
  public void setParameters(Map<String, String> props) {
    this.props = props;
  }
}
