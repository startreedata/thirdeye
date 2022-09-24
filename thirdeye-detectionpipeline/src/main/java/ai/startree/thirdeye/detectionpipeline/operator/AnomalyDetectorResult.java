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
package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Anomaly detection result. Contains a list of anomalies detected and the associated timeseries
 * which includes timestamps, predicted baseline, current value, upper/lower bounds.
 */
public class AnomalyDetectorResult implements OperatorResult {

  private final List<MergedAnomalyResultDTO> anomalies;
  private final TimeSeries timeseries;
  private final Map<String, List> rawData;
  private final EnumerationItemDTO enumerationItem;

  private AnomalyDetectorResult(final List<MergedAnomalyResultDTO> anomalies,
      final TimeSeries timeseries,
      final Map<String, List> rawData,
      final EnumerationItemDTO enumerationItem) {
    this.anomalies = anomalies;
    this.timeseries = timeseries;
    this.rawData = rawData;
    this.enumerationItem = enumerationItem;
  }

  @Override
  public List<MergedAnomalyResultDTO> getAnomalies() {
    return anomalies;
  }

  public TimeSeries getTimeseries() {
    return timeseries;
  }

  public Map<String, List> getRawData() {
    return rawData;
  }

  @Override
  public @Nullable EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  @Override
  public String toString() {
    return "OperatorResult{" + "anomalies=" + anomalies + ", timeseries=" + timeseries + ", rawdata=" + rawData + '}';
  }

  public static class Builder {

    private List<MergedAnomalyResultDTO> anomalies;
    private TimeSeries timeseries;
    private Map<String, List> rawData = Collections.emptyMap();
    private EnumerationItemDTO enumerationItem;

    public Builder setAnomalies(final List<MergedAnomalyResultDTO> anomalies) {
      this.anomalies = anomalies;
      return this;
    }

    public Builder setTimeseries(final TimeSeries timeseries) {
      this.timeseries = timeseries;
      return this;
    }

    public Builder setRawData(final Map<String, List> rawData) {
      this.rawData = rawData;
      return this;
    }

    public Builder setEnumerationItem(
        final EnumerationItemDTO enumerationItem) {
      this.enumerationItem = enumerationItem;
      return this;
    }

    public AnomalyDetectorResult build() {
      return new AnomalyDetectorResult(anomalies, timeseries, rawData, enumerationItem);
    }
  }
}
