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
 */

package org.apache.pinot.thirdeye.detection.components.detectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.detection.components.detectors.results.DataTableUtils.splitDataTable;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries.POSITIVE_INFINITY;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.DoubleFunction;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.map;

import com.google.common.collect.ArrayListMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.components.detectors.results.DimensionInfo;
import org.apache.pinot.thirdeye.detection.components.detectors.results.GroupedDetectionResults;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.InputDataFetcher;
import org.apache.pinot.thirdeye.spi.detection.Pattern;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.InputData;
import org.apache.pinot.thirdeye.spi.detection.model.InputDataSpec;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;

/**
 * Absolute change rule detection
 */
public class AbsoluteChangeRuleDetector implements AnomalyDetector<AbsoluteChangeRuleDetectorSpec>,
    AnomalyDetectorV2<AbsoluteChangeRuleDetectorSpec>,
    BaselineProvider<AbsoluteChangeRuleDetectorSpec> {

  private static final String COL_DIFF = "diff";
  private static final String COL_PATTERN = "pattern";
  private static final String COL_DIFF_VIOLATION = "diff_violation";

  private double absoluteChange;
  private InputDataFetcher dataFetcher;
  private Baseline baseline;
  private Pattern pattern;
  private String monitoringGranularity;
  private TimeGranularity timeGranularity;
  private Period monitoringGranularityPeriod;
  private AbsoluteChangeRuleDetectorSpec spec;

  @Override
  public void init(final AbsoluteChangeRuleDetectorSpec spec) {
    this.spec = spec;

    absoluteChange = spec.getAbsoluteChange();
    final String timezone = spec.getTimezone();
    final String offset = spec.getOffset();
    baseline = BaselineParsingUtils.parseOffset(offset, timezone);
    pattern = Pattern.valueOf(spec.getPattern().toUpperCase());

    monitoringGranularity = spec.getMonitoringGranularity();
    if (monitoringGranularity.equals("1_MONTHS")) {
      timeGranularity = MetricSlice.NATIVE_GRANULARITY;
    } else {
      timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    }
  }

  @Override
  public void init(final AbsoluteChangeRuleDetectorSpec spec, final InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public DetectionPipelineResult runDetection(final Interval window,
      final Map<String, DataTable> timeSeriesMap) throws DetectorException {
    setMonitoringGranularityPeriod();
    final DataTable baseline = timeSeriesMap.get(KEY_BASELINE);
    final DataTable current = timeSeriesMap.get(KEY_CURRENT);
    final Map<DimensionInfo, DataTable> baselineDataTableMap = splitDataTable(baseline);
    final Map<DimensionInfo, DataTable> currentDataTableMap = splitDataTable(current);

    final List<DetectionResult> detectionResults = new ArrayList<>();
    for (DimensionInfo dimensionInfo : baselineDataTableMap.keySet()) {

      final DataFrame currentDf = currentDataTableMap.get(dimensionInfo).getDataFrame();
      final DataFrame baselineDf = baselineDataTableMap.get(dimensionInfo).getDataFrame();

      final DataFrame df = new DataFrame();
      df.addSeries(DataFrame.COL_TIME, currentDf.get(spec.getTimestamp()));
      df.addSeries(DataFrame.COL_CURRENT, currentDf.get(spec.getMetric()));
      df.addSeries(DataFrame.COL_VALUE, baselineDf.get(spec.getMetric()));

      final DetectionResult detectionResult = runDetectionOnSingleDataTable(df, window);
      detectionResults.add(detectionResult);
    }
    return new GroupedDetectionResults(detectionResults);
  }

  private void setMonitoringGranularityPeriod() {
    requireNonNull(spec.getMonitoringGranularity(),
        "monitoringGranularity is mandatory in v2 interface");
    checkArgument(!MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString().equals(
        spec.getMonitoringGranularity()), "NATIVE_GRANULARITY not supported in v2 interface");

    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(spec.getMonitoringGranularity(),
        null);
  }

  @Override
  public DetectionResult runDetection(final Interval window, final String metricUrn) {
    final MetricEntity me = MetricEntity.fromURN(metricUrn);

    final MetricSlice slice = MetricSlice.from(me.getId(),
        window.getStartMillis(),
        window.getEndMillis(),
        me.getFilters(),
        timeGranularity);

    final List<MetricSlice> slices = new ArrayList<>(baseline.scatter(slice));
    slices.add(slice);

    final InputData data = dataFetcher.fetchData(new InputDataSpec()
        .withTimeseriesSlices(slices)
        .withMetricIdsForDataset(singletonList(slice.getMetricId())));

    final DatasetConfigDTO datasetConfig = data.getDatasetForMetricId().get(me.getId());
    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(
        monitoringGranularity,
        datasetConfig);
    // Hack. To be removed when deprecating v1 pipeline
    spec.setTimezone(datasetConfig.getTimezone());

    final DataFrame dfCurr = data
        .getTimeseries()
        .get(slice)
        .renameSeries(DataFrame.COL_VALUE, COL_CURRENT);
    final DataFrame dfBase = baseline.gather(slice, data.getTimeseries());

    // join curr and base
    final DataFrame df = new DataFrame(dfCurr).addSeries(dfBase);
    return runDetectionOnSingleDataTable(df, window);
  }

  private DetectionResult runDetectionOnSingleDataTable(final DataFrame dfInput,
      final ReadableInterval window) {
    // calculate absolute change
    dfInput.addSeries(COL_DIFF, dfInput.getDoubles(COL_CURRENT).subtract(dfInput.get(DataFrame.COL_VALUE)));

    // defaults
    dfInput.addSeries(COL_ANOMALY, BooleanSeries.fillValues(dfInput.size(), false));
    // absolute change
    if (!Double.isNaN(absoluteChange)) {
      // consistent with pattern
      if (pattern.equals(Pattern.UP_OR_DOWN)) {
        dfInput.addSeries(COL_PATTERN, BooleanSeries.fillValues(dfInput.size(), true));
      } else {
        dfInput.addSeries(COL_PATTERN, pattern.equals(Pattern.UP)
            ? dfInput.getDoubles(COL_DIFF).gt(0)
            : dfInput.getDoubles(COL_DIFF).lt(0));
      }
      dfInput.addSeries(COL_DIFF_VIOLATION, dfInput.getDoubles(COL_DIFF).abs().gte(absoluteChange));
      dfInput.mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY, COL_PATTERN, COL_DIFF_VIOLATION);
    }

    // make anomalies
    final MetricSlice slice = MetricSlice.from(-1,
        window.getStartMillis(),
        window.getEndMillis(),
        ArrayListMultimap.create(),
        timeGranularity);

    addAbsoluteChangeBoundaries(dfInput);

    final List<MergedAnomalyResultDTO> anomalies = DetectionUtils.buildAnomalies(slice,
        dfInput,
        spec.getTimezone(),
        monitoringGranularityPeriod);

    return DetectionResult.from(anomalies, TimeSeries.fromDataFrame(dfInput));
  }

  @Override
  public TimeSeries computePredictedTimeSeries(final MetricSlice slice) {
    final DataFrame df = DetectionUtils.buildBaselines(slice, baseline, dataFetcher);
    addAbsoluteChangeBoundaries(df);
    return TimeSeries.fromDataFrame(df);
  }

  private void addAbsoluteChangeBoundaries(final DataFrame dfBase) {
    if (!Double.isNaN(absoluteChange)) {
      switch (pattern) {
        case UP:
          fillAbsoluteChangeBound(dfBase, DataFrame.COL_UPPER_BOUND, absoluteChange);
          dfBase.addSeries(DataFrame.COL_LOWER_BOUND, DoubleSeries.zeros(dfBase.size()));
          break;
        case DOWN:
          dfBase.addSeries(
              DataFrame.COL_UPPER_BOUND, DoubleSeries.fillValues(dfBase.size(), POSITIVE_INFINITY));
          fillAbsoluteChangeBound(dfBase, DataFrame.COL_LOWER_BOUND, -absoluteChange);
          break;
        case UP_OR_DOWN:
          fillAbsoluteChangeBound(dfBase, DataFrame.COL_UPPER_BOUND, absoluteChange);
          fillAbsoluteChangeBound(dfBase, DataFrame.COL_LOWER_BOUND, -absoluteChange);
          break;
        default:
          throw new IllegalArgumentException();
      }
    }
  }

  private void fillAbsoluteChangeBound(final DataFrame dfBase, final String colBound,
      final double change) {
    final Series series = map((DoubleFunction) values -> values[0] + change,
        dfBase.getDoubles(DataFrame.COL_VALUE));
    dfBase.addSeries(colBound, series);
  }
}
