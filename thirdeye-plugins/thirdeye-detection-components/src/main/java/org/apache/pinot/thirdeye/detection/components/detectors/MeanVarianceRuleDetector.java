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
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_DIFF;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_DIFF_VIOLATION;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ERROR;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_PATTERN;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.LongConditional;
import static org.apache.pinot.thirdeye.spi.detection.DetectionUtils.buildDetectionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.components.SimpleAnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
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
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History mean and standard deviation based forecasting and detection.
 * Forecast using history mean and standard deviation.
 */
public class MeanVarianceRuleDetector implements AnomalyDetector<MeanVarianceRuleDetectorSpec>,
    AnomalyDetectorV2<MeanVarianceRuleDetectorSpec>,
    BaselineProvider<MeanVarianceRuleDetectorSpec> {

  private static final Logger LOG = LoggerFactory.getLogger(MeanVarianceRuleDetector.class);

  private InputDataFetcher dataFetcher;
  private Pattern pattern;
  private String monitoringGranularity;
  private TimeGranularity timeGranularity;
  private double sensitivity;
  private int lookback;
  private Period monitoringGranularityPeriod;
  private MeanVarianceRuleDetectorSpec spec;

  /**
   * Mapping of sensitivity to sigma on range of 0.5 - 1.5
   *
   * @param sensitivity double from 0 to 10. Values outside this range are clipped to 0, 10
   * @return sigma
   */
  private static double sigma(final double sensitivity) {
    return 0.5 + 0.1 * (10 - Math.max(Math.min(sensitivity, 10), 0));
  }

  @Override
  public void init(final MeanVarianceRuleDetectorSpec spec) {
    this.spec = spec;
    pattern = spec.getPattern();
    lookback = spec.getLookback();
    sensitivity = spec.getSensitivity();
    monitoringGranularity = spec.getMonitoringGranularity();

    if (monitoringGranularity.equals("1_MONTHS")) {
      timeGranularity = MetricSlice.NATIVE_GRANULARITY;
    } else {
      timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    }

    checkArgument(lookback >= 5,
        String.format("Lookback is %d. Lookback should be greater than 5.", lookback));
  }

  @Override
  public void init(final MeanVarianceRuleDetectorSpec spec, final InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public TimeSeries computePredictedTimeSeries(final MetricSlice slice) {
    // todo cyril - not used - may be broken - logic should be the same for all detectors
    final MetricEntity metricEntity = MetricEntity.fromSlice(slice, 0);
    final Interval window = new Interval(slice.getStart(), slice.getEnd());
    final DateTime trainStart;

    if (isMultiDayGranularity()) {
      trainStart = window.getStart().minusDays(timeGranularity.getSize() * lookback);
    } else if (monitoringGranularity.equals("1_MONTHS")) {
      trainStart = window.getStart().minusMonths(lookback);
    } else {
      trainStart = window.getStart().minusWeeks(lookback);
    }

    final DataFrame inputDf = fetchData(metricEntity,
        trainStart.getMillis(),
        window.getEndMillis());
    DataFrame resultDF = computeBaseline(inputDf, window.getStartMillis());
    resultDF = resultDF.joinLeft(inputDf.renameSeries(
        COL_VALUE, COL_CURRENT), COL_TIME);

    // Exclude the end because baseline calculation should not contain the end
    if (resultDF.size() > 1) {
      resultDF = resultDF.head(resultDF.size() - 1);
    }

    return TimeSeries.fromDataFrame(resultDF);
  }

  @Override
  public DetectionResult runDetection(final Interval window, final String metricUrn) {
    final MetricEntity me = MetricEntity.fromURN(metricUrn);
    final DateTime fetchStart;
    //get historical data
    if (isMultiDayGranularity()) {
      fetchStart = window.getStart().minusDays(timeGranularity.getSize() * lookback);
    } else if (monitoringGranularity.equals("1_MONTHS")) {
      fetchStart = window.getStart().minusMonths(lookback);
    } else {
      fetchStart = window.getStart().minusWeeks(lookback);
    }

    final MetricSlice slice = MetricSlice.from(me.getId(),
        fetchStart.getMillis(),
        window.getEndMillis(),
        me.getFilters(),
        timeGranularity);
    final DatasetConfigDTO datasetConfig = dataFetcher.fetchData(new InputDataSpec()
            .withMetricIdsForDataset(Collections.singleton(me.getId()))).getDatasetForMetricId()
        .get(me.getId());

    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(
        timeGranularity.toAggregationGranularityString(),
        datasetConfig);
    spec.setTimezone(datasetConfig.getTimezone());

    // getting data (window + earliest lookback) all at once.
    LOG.info("Getting data for" + slice);
    final DataFrame dfInput = fetchData(me, fetchStart.getMillis(), window.getEndMillis());
    final AnomalyDetectorV2Result detectorResult = runDetectionOnSingleDataTable(dfInput, window);

    return buildDetectionResult(detectorResult);
  }

  @Override
  public AnomalyDetectorV2Result runDetection(final Interval window,
      final Map<String, DataTable> timeSeriesMap
  ) throws DetectorException {
    setMonitoringGranularityPeriod();
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame currentDf = current.getDataFrame();
    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_VALUE)
        .setIndex(COL_TIME);

    return runDetectionOnSingleDataTable(currentDf, window);
  }

  private void setMonitoringGranularityPeriod() {
    requireNonNull(spec.getMonitoringGranularity(),
        "monitoringGranularity is mandatory in v2 interface");
    checkArgument(!MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString().equals(
        spec.getMonitoringGranularity()), "NATIVE_GRANULARITY not supported in v2 interface");

    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(
        spec.getMonitoringGranularity(),
        null);
  }

  private AnomalyDetectorV2Result runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    final DataFrame baselineDf = computeBaseline(inputDf, window.getStartMillis());
    inputDf
        // rename current which is still called "value" to "current"
        .renameSeries(COL_VALUE, COL_CURRENT)
        // left join baseline values
        .addSeries(baselineDf, COL_VALUE, COL_ERROR, COL_LOWER_BOUND, COL_UPPER_BOUND)
        .addSeries(COL_DIFF, inputDf.getDoubles(COL_CURRENT).subtract(inputDf.get(COL_VALUE)))
        .addSeries(COL_PATTERN, patternMatch(pattern, inputDf))
        .addSeries(COL_DIFF_VIOLATION,
            inputDf.getDoubles(COL_DIFF).abs().gte(inputDf.getDoubles(COL_ERROR)))
        .mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY, COL_PATTERN, COL_DIFF_VIOLATION);

    return new SimpleAnomalyDetectorV2Result(inputDf,
        spec.getTimezone(),
        monitoringGranularityPeriod);
  }

  //todo cyril move this as utils/shared method
  public static BooleanSeries patternMatch(final Pattern pattern, final DataFrame dfInput) {
    // series of boolean that are true if the anomaly direction matches the pattern
    if (pattern.equals(Pattern.UP_OR_DOWN)) {
      return BooleanSeries.fillValues(dfInput.size(), true);
    }
    return pattern.equals(Pattern.UP) ?
        dfInput.getDoubles(COL_DIFF).gt(0) :
        dfInput.getDoubles(COL_DIFF).lt(0);
  }

  private DataFrame computeBaseline(final DataFrame inputDF, final long windowStartTime) {

    final DataFrame resultDF = new DataFrame();
    //filter the data inside window for current values.
    final DataFrame forecastDF = inputDF
        .filter((LongConditional) values -> values[0] >= windowStartTime, COL_TIME)
        .dropNull();

    final int size = forecastDF.size();
    final double[] baselineArray = new double[size];
    final double[] upperBoundArray = new double[size];
    final double[] lowerBoundArray = new double[size];
    final long[] resultTimeArray = new long[size];
    final double[] errorArray = new double[size];

    // todo cyril compute mean and std in a single pass
    // https://nestedsoftware.com/2018/03/20/calculating-a-moving-average-on-streaming-data-5a7k.22879.html
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
    for (int k = 0; k < size; k++) {
      final DataFrame lookbackDf = getLookbackDf(inputDF, forecastDF.getLong(COL_TIME, k));
      double mean = lookbackDf.getDoubles(COL_VALUE).mean().value();
      double std = lookbackDf.getDoubles(COL_VALUE).std().value();
      //calculate baseline, error , upper and lower bound for prediction window.
      resultTimeArray[k] = forecastDF.getLong(COL_TIME, k);
      baselineArray[k] = mean;
      errorArray[k] = sigma(sensitivity) * std;
      upperBoundArray[k] = baselineArray[k] + errorArray[k];
      lowerBoundArray[k] = baselineArray[k] - errorArray[k];
    }
    //Construct the dataframe.
    resultDF
        .addSeries(COL_TIME, LongSeries.buildFrom(resultTimeArray))
        .setIndex(COL_TIME);

    resultDF
        .addSeries(COL_VALUE, DoubleSeries.buildFrom(baselineArray))
        .addSeries(COL_UPPER_BOUND, DoubleSeries.buildFrom(upperBoundArray))
        .addSeries(COL_LOWER_BOUND, DoubleSeries.buildFrom(lowerBoundArray))
        .addSeries(COL_ERROR, DoubleSeries.buildFrom(errorArray));

    return resultDF;
  }

  private DataFrame getLookbackDf(final DataFrame inputDF, final long endTimeMillis) {
    final int indexEnd = inputDF.getLongs(COL_TIME).find(endTimeMillis);
    checkArgument(indexEnd != -1,
        "Could not find index of endTime. endTime should exist in inputDf. This should not happen.");

    DataFrame loobackDf = DataFrame.builder(COL_TIME, COL_VALUE).build();
    final int indexStart = indexEnd - lookback;
    checkArgument(indexStart >= 0,
        "Invalid index. Insufficient data to compute mean/variance on lookback. index: "
            + indexStart);
    loobackDf = loobackDf.append(inputDF.slice(indexStart, indexEnd));
    return loobackDf;
  }

  /**
   * Fetch data from metric
   *
   * @param metricEntity metric entity
   * @param start start timestamp
   * @param end end timestamp
   * @return Data Frame that has data from start to end
   */
  private DataFrame fetchData(final MetricEntity metricEntity, final long start, final long end) {
    final List<MetricSlice> slices = new ArrayList<>();
    final MetricSlice sliceData = MetricSlice.from(metricEntity.getId(), start, end,
        metricEntity.getFilters(), timeGranularity);
    slices.add(sliceData);
    LOG.info("Getting data for" + sliceData);
    final InputData data = dataFetcher.fetchData(new InputDataSpec().withTimeseriesSlices(slices));
    return data.getTimeseries().get(sliceData);
  }

  // Check whether monitoring timeGranularity is multiple days
  private boolean isMultiDayGranularity() {
    return !timeGranularity.equals(MetricSlice.NATIVE_GRANULARITY)
        && timeGranularity.getUnit() == TimeUnit.DAYS;
  }
}
