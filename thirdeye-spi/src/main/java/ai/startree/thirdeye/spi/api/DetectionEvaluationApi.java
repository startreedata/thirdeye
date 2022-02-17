/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DetectionEvaluationApi {

  private Double mape;
  private DetectionDataApi data;
  private List<AnomalyApi> anomalies = new ArrayList<>();

  public Double getMape() {
    return mape;
  }

  public DetectionEvaluationApi setMape(final Double mape) {
    this.mape = mape;
    return this;
  }

  public DetectionDataApi getData() {
    return data;
  }

  public DetectionEvaluationApi setData(final DetectionDataApi data) {
    this.data = data;
    return this;
  }

  public List<AnomalyApi> getAnomalies() {
    return anomalies;
  }

  public DetectionEvaluationApi setAnomalies(
      final List<AnomalyApi> anomalies) {
    this.anomalies = anomalies;
    return this;
  }
}
