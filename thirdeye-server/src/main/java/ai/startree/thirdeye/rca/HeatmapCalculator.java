/*
 * Copyright 2023 StarTree Inc
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
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
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
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeatmapCalculator {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forStyle("LL");
  private static final Logger LOG = LoggerFactory.getLogger(HeatmapCalculator.class);
  private static final long TIMEOUT_MILLIS = 60_000;

  private final RcaInfoFetcher rcaInfoFetcher;
  private final AggregationLoader aggregationLoader;

  @Inject
  public HeatmapCalculator(final RcaInfoFetcher rcaInfoFetcher,
      final AggregationLoader aggregationLoader) {
    this.rcaInfoFetcher = rcaInfoFetcher;
    this.aggregationLoader = aggregationLoader;
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
        rcaInfo.getChronology());

    final Period baselineOffsetPeriod = isoPeriod(baselineOffset);
    final Interval baselineInterval = new Interval(currentInterval.getStart()
        .minus(baselineOffsetPeriod), currentInterval.getEnd().minus(baselineOffsetPeriod));

    // override dimensions
    final DatasetConfigDTO datasetConfigDTO = rcaInfo.getDataset();
    final List<String> rcaDimensions = getRcaDimensions(dimensions,
        excludedDimensions,
        datasetConfigDTO);
    datasetConfigDTO.setDimensions(Templatable.of(rcaDimensions));

    final Map<String, Map<String, Double>> anomalyBreakdown = computeBreakdown(
        rcaInfo.getMetric(),
        parseAndCombinePredicates(filters),
        currentInterval,
        limit,
        datasetConfigDTO);

    final Map<String, Map<String, Double>> baselineBreakdown = computeBreakdown(
        rcaInfo.getMetric(),
        parseAndCombinePredicates(filters),
        baselineInterval,
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
    for (final Entry<String, Map<String, Double>> e : fromBreakdown.entrySet()) {
      final String dimensionName = e.getKey();
      if (!toBreakdown.containsKey(dimensionName)) {
        toBreakdown.put(dimensionName, new HashMap<>());
      }
      final Map<String, Double> fromCounts = e.getValue();
      final Map<String, Double> toCounts = toBreakdown.get(dimensionName);
      for (final String dimensionValue : fromCounts.keySet()) {
        if (!toCounts.containsKey(dimensionValue)) {
          toCounts.put(dimensionValue, 0.);
        }
      }
    }
  }

  public Map<String, Map<String, Double>> computeBreakdown(final MetricConfigDTO metricConfigDTO,
      final List<Predicate> predicates,
      final Interval interval,
      final int limit,
      final DatasetConfigDTO datasetConfigDTO) throws Exception {

    final MetricSlice baseSlice = MetricSlice.from(metricConfigDTO,
        interval,
        predicates,
        datasetConfigDTO);

    LOG.info("RCA metric analysis - Slice: {} - {}",
        DATE_TIME_FORMATTER.print(baseSlice.getInterval().getStartMillis()),
        DATE_TIME_FORMATTER.print(baseSlice.getInterval().getStartMillis()));

    final DataFrame dataBreakdown = aggregationLoader.loadBreakdown(baseSlice, limit);
    final DataFrame dataAggregate = fetchAggregate(baseSlice);

    return DefaultAggregationLoader.makeBreakdownMap(dataBreakdown, dataAggregate);
  }

  /**
   * Returns aggregates for the given set of metric slices.
   *
   * @param slice metric slice
   * @return map of dataframes (keyed by metric slice, columns: [COL_TIME(1), COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private DataFrame fetchAggregate(final MetricSlice slice) throws Exception {
    final Future<DataFrame> future = aggregationLoader.loadAggregateAsync(slice,
        Collections.emptyList(),
        2);
    DataFrame df = future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    if (df.isEmpty()) {
      df = new DataFrame().addSeries(Constants.COL_VALUE, Double.NaN);
    } else if (df.size() > 1) {
      throw new RuntimeException("Aggregation returned more than 1 line.");
    }
    // fill in timestamps
    df.addSeries(Constants.COL_TIME,
            LongSeries.fillValues(df.size(), slice.getInterval().getStartMillis()))
        .setIndex(Constants.COL_TIME);

    return df;
  }
}
