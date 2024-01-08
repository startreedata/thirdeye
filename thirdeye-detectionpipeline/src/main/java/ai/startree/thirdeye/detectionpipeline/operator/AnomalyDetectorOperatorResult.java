/*
 * Copyright 2024 StarTree Inc
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

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.AbstractDataTableImpl;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Anomaly detection result. Contains a list of anomalies detected and the associated timeseries
 * which includes timestamps, predicted baseline, current value, upper/lower bounds.
 */
// todo cyril refactor OperatorResult, AnomalyDetectorOperatorResult, and DataTable interfaces - below keeping the implemented interface for clarity - the abstract class is tech debt
public class AnomalyDetectorOperatorResult extends AbstractDataTableImpl implements OperatorResult, DataTable {

  private final List<AnomalyDTO> anomalies;
  private final TimeSeries timeseries;
  private final Map<String, List> rawData;
  private final EnumerationItemDTO enumerationItem;

  private AnomalyDetectorOperatorResult(final List<AnomalyDTO> anomalies,
      final TimeSeries timeseries,
      final Map<String, List> rawData,
      final EnumerationItemDTO enumerationItem) {
    this.anomalies = anomalies;
    this.timeseries = timeseries;
    this.rawData = rawData;
    this.enumerationItem = enumerationItem;
  }

  public static AnomalyDetectorOperatorResult.Builder builder() {
    return new AnomalyDetectorOperatorResult.Builder();
  }

  @Override
  public List<AnomalyDTO> getAnomalies() {
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
    return "OperatorResult{" + "anomalies=" + anomalies + ", timeseries=" + timeseries
        + ", rawdata=" + rawData + '}';
  }

  @Override
  public DataFrame getDataFrame() {
    return timeseries.getDataFrame();
  }

  public static class Builder {

    private List<AnomalyDTO> anomalies;
    private TimeSeries timeseries;
    private Map<String, List> rawData = Collections.emptyMap();
    private EnumerationItemDTO enumerationItem;

    public Builder setAnomalies(final List<AnomalyDTO> anomalies) {
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

    public AnomalyDetectorOperatorResult build() {
      return new AnomalyDetectorOperatorResult(anomalies, timeseries, rawData, enumerationItem);
    }
  }
}
