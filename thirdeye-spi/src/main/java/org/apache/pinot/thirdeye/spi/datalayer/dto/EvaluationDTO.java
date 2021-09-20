/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.datalayer.dto;

import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.Evaluation;
import org.apache.pinot.thirdeye.spi.detection.PredictionResult;

/**
 * The class for evaluation metrics.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluationDTO extends AbstractDTO {

  private long detectionConfigId; // the detection config id
  private long startTime; // the start time for the detection window being monitored
  private long endTime; // the end time for the detection window being monitored
  private String detectorName; // the name for the detector
  private Double mape; //  the mean absolute percentage error (MAPE)
  private String metricUrn; // the metric urn

  public long getDetectionConfigId() {
    return detectionConfigId;
  }

  public void setDetectionConfigId(long detectionConfigId) {
    this.detectionConfigId = detectionConfigId;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getDetectorName() {
    return detectorName;
  }

  public void setDetectorName(String detectorName) {
    this.detectorName = detectorName;
  }

  public Double getMape() {
    return mape;
  }

  public void setMape(Double mape) {
    this.mape = mape;
  }

  public String getMetricUrn() {
    return metricUrn;
  }

  public void setMetricUrn(String metricUrn) {
    this.metricUrn = metricUrn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvaluationDTO)) {
      return false;
    }
    EvaluationDTO that = (EvaluationDTO) o;
    return detectionConfigId == that.detectionConfigId && startTime == that.startTime
        && endTime == that.endTime
        && Double.compare(that.mape, mape) == 0 && Objects.equals(detectorName, that.detectorName)
        && Objects.equals(
        metricUrn, that.metricUrn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(detectionConfigId, startTime, endTime, detectorName, mape, metricUrn);
  }

  @Override
  public String toString() {
    return "EvaluationBean{" + "detectionConfigId=" + detectionConfigId + ", startTime=" + startTime
        + ", endTime="
        + endTime + ", detectorName='" + detectorName + '\'' + ", mape=" + mape + ", metricUrn='"
        + metricUrn + '\''
        + '}';
  }


  public static EvaluationDTO fromPredictionResult(PredictionResult predictionResult,
      long startTime, long endTime,
      long detectionConfigId) {
    EvaluationDTO evaluation = new EvaluationDTO();
    evaluation.setDetectionConfigId(detectionConfigId);
    evaluation.setStartTime(startTime);
    evaluation.setEndTime(endTime);
    evaluation.setDetectorName(predictionResult.getDetectorName());
    evaluation.setMetricUrn(predictionResult.getMetricUrn());
    evaluation.setMape(getMape(predictionResult));
    return evaluation;
  }

  private static Double getMape(PredictionResult result) {
    DataFrame df = result.getPredictedTimeSeries();
    // drop zero current value for mape calculation
    df = df.filter(df.getDoubles(COL_CURRENT).ne(0.0)).dropNull(COL_CURRENT, COL_VALUE);
    Double mape = Evaluation.calculateMape(df.getDoubles(COL_CURRENT), df.getDoubles(COL_VALUE));
    if (Double.isNaN(mape)) {
      // explicitly swap NaN to null values because mysql doesn't support storing NaN and will throw an exception.
      mape = null;
    }
    return mape;
  }
}
