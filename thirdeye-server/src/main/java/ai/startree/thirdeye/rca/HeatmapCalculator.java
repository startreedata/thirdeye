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

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.rca.RcaDimensionFilterHelper.getRcaDimensions;
import static ai.startree.thirdeye.spi.datalayer.Predicate.parseAndCombinePredicates;

import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
import ai.startree.thirdeye.rootcause.BaselineAggregate;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi.HeatMapBreakdownApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.detection.Baseline;
import ai.startree.thirdeye.spi.detection.BaselineAggregateType;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeatmapCalculator {

  private static final Logger LOG = LoggerFactory.getLogger(HeatmapCalculator.class);
  private static final long TIMEOUT = 600000;

  private final ExecutorService executor;
  private final RcaInfoFetcher rcaInfoFetcher;
  private final AggregationLoader aggregationLoader;

  @Inject
  public HeatmapCalculator(final RcaInfoFetcher rcaInfoFetcher,
      final AggregationLoader aggregationLoader) {
    this.rcaInfoFetcher = rcaInfoFetcher;
    this.aggregationLoader = aggregationLoader;

    this.executor = Executors.newCachedThreadPool();
  }

  private static void logSlices(MetricSlice baseSlice, List<MetricSlice> slices) {
    final DateTimeFormatter formatter = DateTimeFormat.forStyle("LL");
    LOG.info("RCA metric analysis - Base slice: {} - {}",
        formatter.print(baseSlice.getStartMillis()),
        formatter.print(baseSlice.getEndMillis()));
    for (int i = 0; i < slices.size(); i++) {
      LOG.info("RCA metric analysis - Offset Slice {}:  {} - {}",
          i,
          formatter.print(slices.get(i).getStartMillis()),
          formatter.print(slices.get(i).getEndMillis()));
    }
  }

  /**
   * Returns a baseline equivalent to "current" in the wo1w format.
   * Ie when used for a scatter/gather operation, this baseline will only generate one slice,
   * on the startTime/endTime provided.
   *
   * Hack to keep the compatibility with complex baselines.
   * May be removed once timeseries filtering and timeseries baseline is implemented.
   */
  public static Baseline getSimpleRange() {
    return BaselineAggregate.fromWeekOverWeek(BaselineAggregateType.SUM, 1, 0, DateTimeZone.UTC);
  }

  public HeatMapResponseApi compute(final long anomalyId,
      final String baselineOffset,
      final List<String> filters,
      final Integer limit,
      final List<String> dimensions,
      final List<String> excludedDimensions) throws Exception {
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(
        anomalyId);
    final Interval currentInterval = new Interval(rcaInfo.getAnomaly()
        .getStartTime(),
        rcaInfo.getAnomaly().getEndTime(),
        rcaInfo.getTimezone());

    Period baselineOffsetPeriod = Period.parse(baselineOffset, ISOPeriodFormat.standard());
    final Interval baselineInterval = new Interval(currentInterval.getStart()
        .minus(baselineOffsetPeriod), currentInterval.getEnd().minus(baselineOffsetPeriod));

    // override dimensions
    final DatasetConfigDTO datasetConfigDTO = rcaInfo.getDataset();
    List<String> rcaDimensions = getRcaDimensions(dimensions,
        excludedDimensions,
        datasetConfigDTO);
    datasetConfigDTO.setDimensions(Templatable.of(rcaDimensions));

    final Map<String, Map<String, Double>> anomalyBreakdown = computeBreakdown(
        rcaInfo.getMetric(),
        parseAndCombinePredicates(filters),
        currentInterval,
        getSimpleRange(),
        limit,
        datasetConfigDTO);

    final Map<String, Map<String, Double>> baselineBreakdown = computeBreakdown(
        rcaInfo.getMetric(),
        parseAndCombinePredicates(filters),
        baselineInterval,
        getSimpleRange(),
        limit,
        datasetConfigDTO);

    // if a dimension value is not observed in a breakdown but observed in the other, add it with a count of 0
    fillMissingKeysWithZeroes(baselineBreakdown, anomalyBreakdown);
    fillMissingKeysWithZeroes(anomalyBreakdown, baselineBreakdown);

    return new HeatMapResponseApi()
        .setMetric(new MetricApi()
            .setName(rcaInfo.getMetric().getName())
            .setDataset(new DatasetApi().setName(datasetConfigDTO.getDataset())))
        .setCurrent(new HeatMapBreakdownApi().setBreakdown(anomalyBreakdown))
        .setBaseline(new HeatMapBreakdownApi().setBreakdown(baselineBreakdown));
  }

  /**
   * Inserts all keys that are present in fromBreakdown but absent in toBreakdown, with a value of
   * 0.
   */
  private void fillMissingKeysWithZeroes(final Map<String, Map<String, Double>> fromBreakdown,
      final Map<String, Map<String, Double>> toBreakdown) {
    // all keys that are present in fromBreakdown but not in toBreakdown are inserted with a value of 0
    for (String dimensionName : fromBreakdown.keySet()) {
      if (!toBreakdown.containsKey(dimensionName)) {
        toBreakdown.put(dimensionName, new HashMap<>());
      }
      Map<String, Double> fromCounts = fromBreakdown.get(dimensionName);
      Map<String, Double> toCounts = toBreakdown.get(dimensionName);
      for (String dimensionValue : fromCounts.keySet()) {
        if (!toCounts.containsKey(dimensionValue)) {
          toCounts.put(dimensionValue, 0.);
        }
      }
    }
  }

  public Map<String, Map<String, Double>> computeBreakdown(final MetricConfigDTO metricConfigDTO,
      final List<Predicate> predicates,
      final Interval interval,
      final Baseline range,
      final int limit,
      final DatasetConfigDTO datasetConfigDTO) throws Exception {

    MetricSlice baseSlice = MetricSlice.from(metricConfigDTO,
        interval,
        predicates,
        datasetConfigDTO);

    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> dataBreakdown = fetchBreakdowns(slices, limit);
    Map<MetricSlice, DataFrame> dataAggregate = fetchAggregates(slices);

    DataFrame resultBreakdown = range.gather(baseSlice, dataBreakdown);
    DataFrame resultAggregate = range.gather(baseSlice, dataAggregate);

    return DefaultAggregationLoader.makeBreakdownMap(resultBreakdown, resultAggregate);
  }

  /**
   * Returns aggregates for the given set of metric slices.
   *
   * @param slices metric slices
   * @return map of dataframes (keyed by metric slice, columns: [COL_TIME(1), COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private Map<MetricSlice, DataFrame> fetchAggregates(List<MetricSlice> slices) throws Exception {
    Map<MetricSlice, Future<DataFrame>> futures = new HashMap<>();
    for (final MetricSlice slice : slices) {
      futures.put(slice, aggregationLoader.loadAggregateAsync(slice, Collections.emptyList(), 2));
    }

    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<DataFrame>> entry : futures.entrySet()) {
      final MetricSlice slice = entry.getKey();
      DataFrame df = entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS);
      if (df.isEmpty()) {
        df = new DataFrame().addSeries(Constants.COL_VALUE, Double.NaN);
      } else if (df.size() > 1) {
        throw new RuntimeException("Aggregation returned more than 1 line.");
      }
      // fill in timestamps
      df.addSeries(Constants.COL_TIME,
              LongSeries.fillValues(df.size(), slice.getInterval().getStartMillis()))
          .setIndex(Constants.COL_TIME);

      output.put(entry.getKey(), df);
    }

    return output;
  }

  /**
   * Returns breakdowns (de-aggregations) for a given set of metric slices.
   *
   * @param slices metric slices
   * @param limit top k elements limit
   * @return map of dataframes (keyed by metric slice,
   *     columns: [COL_TIME(1), COL_DIMENSION_NAME, COL_DIMENSION_VALUE, COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private Map<MetricSlice, DataFrame> fetchBreakdowns(List<MetricSlice> slices, final int limit)
      throws Exception {
    Map<MetricSlice, Future<DataFrame>> futures = new HashMap<>();
    for (final MetricSlice slice : slices) {
      futures.put(slice, this.executor.submit(() -> aggregationLoader.loadBreakdown(slice, limit)));
    }

    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<DataFrame>> entry : futures.entrySet()) {
      output.put(entry.getKey(), entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    return output;
  }
}
