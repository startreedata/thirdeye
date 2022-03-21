/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.download;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.HashMap;
import java.util.Map;

public class ModelDownloaderConfiguration {

  private TimeGranularity runFrequency;
  private String className;
  private String destinationPath;
  private Map<String, Object> properties = new HashMap<>();

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public TimeGranularity getRunFrequency() {
    return runFrequency;
  }

  public void setRunFrequency(TimeGranularity runFrequency) {
    this.runFrequency = runFrequency;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }
}
