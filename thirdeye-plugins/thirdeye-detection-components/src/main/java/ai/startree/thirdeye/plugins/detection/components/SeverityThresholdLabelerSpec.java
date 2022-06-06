/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SeverityThresholdLabelerSpec extends AbstractSpec {

  public static final String CHANGE_KEY = "change";
  public static final String DURATION_KEY = "duration";

  private Map<String, Map<String, Object>> severity;

  public Map<String, Map<String, Object>> getSeverity() {
    return severity;
  }

  public void setSeverity(Map<String, Map<String, Object>> severity) {
    this.severity = severity;
  }
}
