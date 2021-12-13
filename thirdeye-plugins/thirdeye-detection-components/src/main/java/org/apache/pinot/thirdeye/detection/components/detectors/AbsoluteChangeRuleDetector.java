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
import static org.apache.pinot.thirdeye.detection.components.detectors.MeanVarianceRuleDetector.patternMatch;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_DIFF;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_DIFF_VIOLATION;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_PATTERN;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries.POSITIVE_INFINITY;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.DOWN;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.UP;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.UP_OR_DOWN;

import com.google.common.collect.ArrayListMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
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
    checkArgument(!Double.isNaN(spec.getAbsoluteChange()), "Absolute change is not set.");
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
    final DataTable baseline = requireNonNull(timeSeriesMap.get(KEY_BASELINE), "baseline is null");
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame baselineDf = baseline.getDataFrame();
    final DataFrame currentDf = current.getDataFrame();

    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_CURRENT)
        .setIndex(COL_TIME)
        .addSeries(COL_VALUE, baselineDf.get(spec.getMetric()));

    return runDetectionOnSingleDataTable(currentDf, window);
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
        .renameSeries(COL_VALUE, COL_CURRENT);
    final DataFrame dfBase = baseline.gather(slice, data.getTimeseries());

    // join curr and base
    final DataFrame df = new DataFrame(dfCurr).addSeries(dfBase);
    return runDetectionOnSingleDataTable(df, window);
  }

  private DetectionResult runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    // calculate absolute change
    inputDf
        .addSeries(COL_DIFF, inputDf.getDoubles(COL_CURRENT).subtract(inputDf.get(COL_VALUE)))
        .addSeries(COL_PATTERN, patternMatch(pattern, inputDf))
        .addSeries(COL_DIFF_VIOLATION, inputDf.getDoubles(COL_DIFF).abs().gte(absoluteChange))
        .mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY, COL_PATTERN, COL_DIFF_VIOLATION);
    addBoundaries(inputDf);

    return getDetectionResultTemp(inputDf, window);
  }

  private DetectionResult getDetectionResultTemp(final DataFrame inputDf,
      final ReadableInterval window) {
    // make anomalies
    final MetricSlice slice = MetricSlice.from(-1,
        window.getStartMillis(),
        window.getEndMillis(),
        ArrayListMultimap.create(),
        timeGranularity);

    final List<MergedAnomalyResultDTO> anomalies = DetectionUtils.buildAnomalies(slice,
        inputDf,
        spec.getTimezone(),
        monitoringGranularityPeriod);

    return DetectionResult.from(anomalies, TimeSeries.fromDataFrame(inputDf.sortedBy(COL_TIME)));
  }

  @Override
  public TimeSeries computePredictedTimeSeries(final MetricSlice slice) {
    final DataFrame df = DetectionUtils.buildBaselines(slice, baseline, dataFetcher);
    addBoundaries(df);
    return TimeSeries.fromDataFrame(df);
  }

  private void addBoundaries(final DataFrame inputDf) {
    //default bounds
    DoubleSeries upperBound = DoubleSeries.fillValues(inputDf.size(), POSITIVE_INFINITY);
    //fixme cyril this not consistent with threshold rule detector default values
    DoubleSeries lowerBound = DoubleSeries.zeros(inputDf.size());
    if (pattern == UP || pattern == UP_OR_DOWN) {
      upperBound = inputDf.getDoubles(COL_VALUE).add(absoluteChange);
    }
    if (pattern == DOWN || pattern == UP_OR_DOWN) {
      lowerBound = inputDf.getDoubles(COL_VALUE).add(-absoluteChange);
    }
    inputDf.addSeries(COL_UPPER_BOUND, upperBound);
    inputDf.addSeries(COL_LOWER_BOUND, lowerBound);
  }
}
