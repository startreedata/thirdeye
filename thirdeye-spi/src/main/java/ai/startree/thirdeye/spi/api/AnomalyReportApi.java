/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

public class AnomalyReportApi implements ThirdEyeApi {
  private AnomalyApi anomaly;
  private String url;

  private AnomalyReportDataApi data;

  public AnomalyApi getAnomaly() {
    return anomaly;
  }

  public AnomalyReportApi setAnomaly(final AnomalyApi anomaly) {
    this.anomaly = anomaly;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public AnomalyReportApi setUrl(final String url) {
    this.url = url;
    return this;
  }

  public AnomalyReportDataApi getData() {
    return data;
  }

  public AnomalyReportApi setData(final AnomalyReportDataApi data) {
    this.data = data;
    return this;
  }
}
