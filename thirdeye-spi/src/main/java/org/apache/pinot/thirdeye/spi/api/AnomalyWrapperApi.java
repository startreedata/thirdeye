package org.apache.pinot.thirdeye.spi.api;

public class AnomalyWrapperApi implements ThirdEyeApi {
  private AnomalyApi anomaly;
  private String url;

  public AnomalyApi getAnomaly() {
    return anomaly;
  }

  public AnomalyWrapperApi setAnomaly(final AnomalyApi anomaly) {
    this.anomaly = anomaly;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public AnomalyWrapperApi setUrl(final String url) {
    this.url = url;
    return this;
  }
}
