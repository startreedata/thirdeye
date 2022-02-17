/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

public class AnomalyFetcherConfig {

  private String type;
  private AnomalySource anomalySourceType;
  private String anomalySource;
  private String properties;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public AnomalySource getAnomalySourceType() {
    return anomalySourceType;
  }

  public void setAnomalySourceType(AnomalySource anomalySourceType) {
    this.anomalySourceType = anomalySourceType;
  }

  public String getAnomalySource() {
    return anomalySource;
  }

  public void setAnomalySource(String anomalySource) {
    this.anomalySource = anomalySource;
  }

  public String getProperties() {
    return properties;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }
}
