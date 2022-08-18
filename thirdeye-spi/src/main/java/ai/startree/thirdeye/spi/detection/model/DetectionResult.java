/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.spi.detection.model;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The detection result. Contains a list of anomalies detected and time series
 * (which can include time stamps, predicted baseline, current value, upper/lower bounds)
 */
public class DetectionResult implements DetectionPipelineResult {

  private final List<MergedAnomalyResultDTO> anomalies;
  private final TimeSeries timeseries;
  private final Map<String, List> rawData;

  private DetectionResult(final List<MergedAnomalyResultDTO> anomalies, final TimeSeries timeseries) {
    this(anomalies, timeseries, Collections.emptyMap());
  }

  private DetectionResult(final List<MergedAnomalyResultDTO> anomalies, final TimeSeries timeseries, final Map<String, List> rawData) {
    this.anomalies = anomalies;
    this.timeseries = timeseries;
    this.rawData = rawData;
  }

  /**
   * TODO spyne refactor. this is just to accomodate the needs of DetectionPipelineResult Interface.
   * This needs  cleanup.
   * @param source
   */
  protected DetectionResult(final DetectionResult source) {
    this(source.getAnomalies(), source.getTimeseries(), source.getRawData());
  }
  /**
   * Create a empty detection result
   *
   * @return the empty detection result
   */
  public static DetectionResult empty() {
    return new DetectionResult(Collections.emptyList(), TimeSeries.empty());
  }

  /**
   * Create a detection result from a list of anomalies
   *
   * @param anomalies the list of anomalies generated
   * @return the detection result contains the list of anomalies
   */
  public static DetectionResult from(final List<MergedAnomalyResultDTO> anomalies) {
    return new DetectionResult(anomalies, TimeSeries.empty());
  }

  /**
   * Create a detection result from a list of anomalies and time series
   *
   * @param anomalies the list of anomalies generated
   * @param timeSeries the time series which including the current, predicted baseline and
   *     optionally upper and lower bounds
   * @return the detection result contains the list of anomalies and the time series
   */
  public static DetectionResult from(final List<MergedAnomalyResultDTO> anomalies,
      final TimeSeries timeSeries) {
    return new DetectionResult(anomalies, timeSeries);
  }

  /**
   * Create a detection result from the raw data table
   *
   * @param rawData the raw data map with column name and the values.
   * @return the detection result contains the raw data table
   */
  public static DetectionResult from(final Map<String, List> rawData) {
    return new DetectionResult(Collections.emptyList(), TimeSeries.empty(), rawData);
  }

  public static DetectionResult from(final DataFrame dataFrame) {
    final Map<String, List> rawData = new HashMap<>();
    dataFrame.getSeriesNames().forEach(name -> rawData.put(name, dataFrame.getSeries().get(name).getObjects().toListTyped()));
    return from(rawData);
  }

  @Override
  public List<MergedAnomalyResultDTO> getAnomalies() {
    return anomalies;
  }

  public TimeSeries getTimeseries() {
    return timeseries;
  }

  @Override
  public String toString() {
    return "DetectionResult{" + "anomalies=" + anomalies + ", timeseries=" + timeseries + ", rawdata=" + rawData + '}';
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    return ImmutableList.of(this);
  }

  public Map<String, List> getRawData() {
    return rawData;
  }
}
