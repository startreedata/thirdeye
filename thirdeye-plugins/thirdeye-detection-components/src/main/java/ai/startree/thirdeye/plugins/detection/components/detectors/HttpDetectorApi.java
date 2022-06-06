/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components.detectors;

public class HttpDetectorApi {

  private Long startMillis;
  private Long endMillis;
  private RemoteHttpDetectorSpec spec;
  private DataFrameApi dataframe;

  public Long getStartMillis() {
    return startMillis;
  }

  public HttpDetectorApi setStartMillis(final Long startMillis) {
    this.startMillis = startMillis;
    return this;
  }

  public Long getEndMillis() {
    return endMillis;
  }

  public HttpDetectorApi setEndMillis(final Long endMillis) {
    this.endMillis = endMillis;
    return this;
  }

  public RemoteHttpDetectorSpec getSpec() {
    return spec;
  }

  public HttpDetectorApi setSpec(
      final RemoteHttpDetectorSpec spec) {
    this.spec = spec;
    return this;
  }

  public DataFrameApi getDataframe() {
    return dataframe;
  }

  public HttpDetectorApi setDataframe(
      final DataFrameApi dataframe) {
    this.dataframe = dataframe;
    return this;
  }
}
