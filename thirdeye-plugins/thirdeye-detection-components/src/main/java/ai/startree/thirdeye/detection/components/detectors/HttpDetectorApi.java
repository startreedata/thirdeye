/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import ai.startree.thirdeye.spi.api.DetectionDataApi;

public class HttpDetectorApi {

  private Long startMillis;
  private Long endMillis;
  private RemoteHttpDetectorSpec spec;
  private DataFrameApi dataframe;
  private DetectionDataApi data;

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

  public DetectionDataApi getData() {
    return data;
  }

  public HttpDetectorApi setData(final DetectionDataApi data) {
    this.data = data;
    return this;
  }
}
