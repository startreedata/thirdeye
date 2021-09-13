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

package org.apache.pinot.thirdeye.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.datasource.MetricExpression;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.StringSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.MetricFunction;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponseRow;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

/**
 * Utility class for ThirdEye-specific parsers and transformers of data related to DataFrame.
 */
public class DataFrameUtils {

  /**
   * Returns a Thirdeye response parsed as a DataFrame. The method stores the time values in
   * {@code COL_TIME} by default, and creates columns for each groupBy attribute and for each
   * MetricFunction specified in the request.
   *
   * @param response thirdeye client response
   * @return response as dataframe
   */
  public static DataFrame parseResponse(ThirdEyeResponse response) {
    // builders
    LongSeries.Builder timeBuilder = LongSeries.builder();
    List<StringSeries.Builder> dimensionBuilders = new ArrayList<>();
    List<DoubleSeries.Builder> functionBuilders = new ArrayList<>();

    for (int i = 0; i < response.getGroupKeyColumns().size(); i++) {
      dimensionBuilders.add(StringSeries.builder());
    }

    for (int i = 0; i < response.getMetricFunctions().size(); i++) {
      functionBuilders.add(DoubleSeries.builder());
    }

    // values
    for (int i = 0; i < response.getNumRows(); i++) {
      ThirdEyeResponseRow r = response.getRow(i);
      timeBuilder.addValues(r.getTimeBucketId());

      for (int j = 0; j < r.getDimensions().size(); j++) {
        dimensionBuilders.get(j).addValues(r.getDimensions().get(j));
      }

      for (int j = 0; j < r.getMetrics().size(); j++) {
        functionBuilders.get(j).addValues(r.getMetrics().get(j));
      }
    }

    // dataframe
    String timeColumn = response.getDataTimeSpec().getColumnName();

    DataFrame df = new DataFrame();
    df.addSeries(DataFrame.COL_TIME, timeBuilder.build());
    df.setIndex(DataFrame.COL_TIME);

    int i = 0;
    for (String n : response.getGroupKeyColumns()) {
      if (!timeColumn.equals(n)) {
        df.addSeries(n, dimensionBuilders.get(i++).build());
      }
    }

    int j = 0;
    for (MetricFunction mf : response.getMetricFunctions()) {
      df.addSeries(mf.toString(), functionBuilders.get(j++).build());
    }

    // compression
    for (String name : df.getSeriesNames()) {
      if (Series.SeriesType.STRING.equals(df.get(name).type())) {
        df.addSeries(name, df.getStrings(name).compress());
      }
    }

    return df.sortedBy(DataFrame.COL_TIME);
  }

  /**
   * Returns the DataFrame augmented with a {@code COL_VALUE} column that contains the
   * evaluation results from computing derived metric expressions. The method performs the
   * augmentation in-place.
   *
   * <br/><b>NOTE:</b> only supports computation of a single MetricExpression.
   *
   * @param df thirdeye response dataframe
   * @param expressions collection of metric expressions
   * @param thirdEyeCacheRegistry
   * @return augmented dataframe
   * @throws Exception if the metric expression cannot be computed
   */
  public static DataFrame evaluateExpressions(DataFrame df,
      Collection<MetricExpression> expressions,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) throws Exception {
    if (expressions.size() != 1) {
      throw new IllegalArgumentException("Requires exactly one expression");
    }

    MetricExpression me = expressions.iterator().next();
    Collection<MetricFunction> functions = me.computeMetricFunctions(
        thirdEyeCacheRegistry);

    Map<String, Double> context = new HashMap<>();
    double[] values = new double[df.size()];

    for (int i = 0; i < df.size(); i++) {
      for (MetricFunction f : functions) {
        // TODO check inconsistency between getMetricName() and toString()
        context.put(f.getMetricName(), df.getDouble(f.toString(), i));
      }
      values[i] = MetricExpression.evaluateExpression(me, context);
    }

    // drop intermediate columns
    for (MetricFunction f : functions) {
      df.dropSeries(f.toString());
    }

    return df.addSeries(DataFrame.COL_VALUE, values);
  }

  /**
   * Returns the DataFrame with timestamps aligned to a start offset and an interval.
   *
   * @param df thirdeye response dataframe
   * @param origin start offset
   * @param interval timestep multiple
   * @return dataframe with modified timestamps
   */
  public static DataFrame makeTimestamps(DataFrame df, final DateTime origin,
      final Period interval) {
    return new DataFrame(df).mapInPlace(new Series.LongFunction() {
      @Override
      public long apply(long... values) {
        return origin.plus(interval.multipliedBy((int) values[0])).getMillis();
      }
    }, DataFrame.COL_TIME);
  }

  /**
   * Returns a Thirdeye response parsed as a DataFrame. The method stores the time values in
   * {@code COL_TIME} by default, and creates columns for each groupBy attribute and for each
   * MetricFunction specified in the request. It further evaluates expressions for derived
   * metrics.
   *
   * @param response thirdeye client response
   * @param rc RequestContainer
   * @param thirdEyeCacheRegistry
   * @return response as dataframe
   */
  public static DataFrame evaluateResponse(ThirdEyeResponse response, RequestContainer rc,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    return evaluateExpressions(parseResponse(response), rc.getExpressions(),
        thirdEyeCacheRegistry);
  }

  /**
   * Returns a Thirdeye response parsed as a DataFrame. The method stores the time values in
   * {@code COL_TIME} by default, and creates columns for each groupBy attribute and for each
   * MetricFunction specified in the request. It evaluates expressions for derived
   * metrics and offsets timestamp based on the original timeseries request.
   *
   * @param response thirdeye client response
   * @param rc TimeSeriesRequestContainer
   * @param thirdEyeCacheRegistry
   * @return response as dataframe
   */
  public static DataFrame evaluateResponse(ThirdEyeResponse response, TimeSeriesRequestContainer rc,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    return makeTimestamps(evaluateExpressions(parseResponse(response), rc.getExpressions(),
        thirdEyeCacheRegistry),
        rc.start, rc.getInterval());
  }

  /**
   * Returns a map-transformation of a given DataFrame, assuming that all values can be converted
   * to Double values. The map is keyed by series names.
   *
   * @param df dataframe
   * @return map transformation of dataframe
   */
  public static Map<String, List<Double>> toMap(DataFrame df) {
    Map<String, List<Double>> map = new HashMap<>();
    for (String series : df.getSeriesNames()) {
      map.put(series, df.getDoubles(series).toList());
    }
    return map;
  }

  /**
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database. Also aligns start and end timestamps by
   * rounding them down (start) and up (end) to align with metric time granularity boundaries.
   * <br/><b>NOTE:</b> the aligned end timestamp is still exclusive.
   *
   * @param slice metric data slice
   * @param reference unique identifier for request
   * @param metricDAO metric config DAO
   * @param datasetDAO dataset config DAO
   * @param thirdEyeCacheRegistry
   * @return TimeSeriesRequestContainer
   */
  public static TimeSeriesRequestContainer makeTimeSeriesRequestAligned(MetricSlice slice,
      String reference, MetricConfigManager metricDAO, DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    MetricConfigDTO metric = metricDAO.findById(slice.metricId);
    if (metric == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric id %d", slice.metricId));
    }

    DatasetConfigDTO dataset = datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(String
          .format("Could not resolve dataset '%s' for metric id '%d'", metric.getDataset(),
              metric.getId()));
    }

    List<MetricExpression> expressions = Utils.convertToMetricExpressions(metric.getName(),
        metric.getDefaultAggFunction(), metric.getDataset(),
        thirdEyeCacheRegistry);

    TimeGranularity granularity = dataset.bucketTimeGranularity();
    if (!MetricSlice.NATIVE_GRANULARITY.equals(slice.granularity)) {
      granularity = slice.granularity;
    }

    DateTimeZone timezone = DateTimeZone.forID(dataset.getTimezone());
    Period period = granularity.toPeriod();

    DateTime start = new DateTime(slice.start, timezone)
        .withFields(SpiUtils.makeOrigin(period.getPeriodType()));
    DateTime end = new DateTime(slice.end, timezone).withFields(SpiUtils.makeOrigin(period.getPeriodType()));

    MetricSlice alignedSlice = MetricSlice
        .from(slice.metricId, start.getMillis(), end.getMillis(), slice.filters, slice.granularity);

    ThirdEyeRequest request = makeThirdEyeRequestBuilder(alignedSlice, dataset, expressions,
        thirdEyeCacheRegistry
    )
        .setGroupByTimeGranularity(granularity)
        .build(reference);

    return new TimeSeriesRequestContainer(request, expressions, start, end, granularity.toPeriod());
  }

  /**
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database.
   *
   * @param slice metric data slice
   * @param reference unique identifier for request
   * @param metricDAO metric config DAO
   * @param datasetDAO dataset config DAO
   * @param thirdEyeCacheRegistry
   * @return TimeSeriesRequestContainer
   */
  public static TimeSeriesRequestContainer makeTimeSeriesRequest(MetricSlice slice,
      String reference, MetricConfigManager metricDAO, DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    MetricConfigDTO metric = metricDAO.findById(slice.metricId);
    if (metric == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric id %d", slice.metricId));
    }

    DatasetConfigDTO dataset = datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(String
          .format("Could not resolve dataset '%s' for metric id '%d'", metric.getDataset(),
              metric.getId()));
    }

    List<MetricExpression> expressions = Utils.convertToMetricExpressions(metric.getName(),
        metric.getDefaultAggFunction(), metric.getDataset(),
        thirdEyeCacheRegistry);

    TimeGranularity granularity = dataset.bucketTimeGranularity();
    if (!MetricSlice.NATIVE_GRANULARITY.equals(slice.granularity)) {
      granularity = slice.granularity;
    }

    DateTimeZone timezone = DateTimeZone.forID(dataset.getTimezone());
    Period period = granularity.toPeriod();

    DateTime start = new DateTime(slice.start, timezone)
        .withFields(SpiUtils.makeOrigin(period.getPeriodType()));
    DateTime end = new DateTime(slice.end, timezone).withFields(SpiUtils.makeOrigin(period.getPeriodType()));

    ThirdEyeRequest request = makeThirdEyeRequestBuilder(slice, dataset, expressions,
        thirdEyeCacheRegistry
    )
        .setGroupByTimeGranularity(granularity)
        .build(reference);

    return new TimeSeriesRequestContainer(request, expressions, start, end, granularity.toPeriod());
  }

  /**
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database.
   *
   * @param slice metric data slice
   * @param dimensions dimensions to group by
   * @param limit top k element limit ({@code -1} for default)
   * @param reference unique identifier for request
   * @param metricDAO metric config DAO
   * @param datasetDAO dataset config DAO
   * @param thirdEyeCacheRegistry
   * @return RequestContainer
   */
  public static RequestContainer makeAggregateRequest(MetricSlice slice,
      List<String> dimensions,
      int limit,
      String reference,
      MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    MetricConfigDTO metric = metricDAO.findById(slice.metricId);
    if (metric == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric id %d", slice.metricId));
    }

    DatasetConfigDTO dataset = datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(String
          .format("Could not resolve dataset '%s' for metric id '%d'", metric.getDataset(),
              metric.getId()));
    }

    List<MetricExpression> expressions = Utils.convertToMetricExpressions(metric.getName(),
        metric.getDefaultAggFunction(), metric.getDataset(),
        thirdEyeCacheRegistry);

    ThirdEyeRequest request = makeThirdEyeRequestBuilder(slice, dataset, expressions,
        thirdEyeCacheRegistry
    )
        .setGroupBy(dimensions)
        .setLimit(limit)
        .build(reference);

    return new RequestContainer(request, expressions);
  }

  /**
   * Returns the series with the period added, given the timezone
   *
   * @param s series
   * @param period time period
   * @param timezone time zone
   * @return offset time series
   */
  public static LongSeries addPeriod(Series s, final Period period, final DateTimeZone timezone) {
    return s.map(new Series.LongFunction() {
      @Override
      public long apply(long... values) {
        return new DateTime(values[0], timezone).plus(period).getMillis();
      }
    });
  }

  /**
   * Helper: Returns a pre-populated ThirdeyeRequestBuilder instance. Removes invalid filter values.
   *
   * @param slice metric data slice
   * @param dataset dataset dto
   * @param expressions metric expressions
   * @param thirdEyeCacheRegistry
   * @return ThirdeyeRequestBuilder
   */
  private static ThirdEyeRequest.ThirdEyeRequestBuilder makeThirdEyeRequestBuilder(
      MetricSlice slice,
      DatasetConfigDTO dataset,
      List<MetricExpression> expressions,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    List<MetricFunction> functions = new ArrayList<>();
    for (MetricExpression exp : expressions) {
      functions.addAll(exp.computeMetricFunctions(
          thirdEyeCacheRegistry));
    }

    return ThirdEyeRequest.newBuilder()
        .setStartTimeInclusive(slice.start)
        .setEndTimeExclusive(slice.end)
        .setFilterSet(slice.filters)
        .setMetricFunctions(functions)
        .setDataSource(dataset.getDataSource());
  }

  private static Series makeSelectionSeries(ThirdEyeResultSet resultSet, int colIndex) {
    int rowCount = resultSet.getRowCount();
    if (rowCount <= 0) {
      return StringSeries.empty();
    }

    String[] values = new String[rowCount];
    for (int i = 0; i < rowCount; i++) {
      values[i] = resultSet.getString(i, colIndex);
    }

    return DataFrame.toSeries(values);
  }

  private static Series makeGroupByValueSeries(ThirdEyeResultSet resultSet) {
    int rowCount = resultSet.getRowCount();
    if (rowCount <= 0) {
      return StringSeries.empty();
    }

    String[] values = new String[rowCount];
    for (int i = 0; i < rowCount; i++) {
      values[i] = resultSet.getString(i, 0);
    }

    return DataFrame.toSeries(values);
  }

  private static Series makeGroupByGroupSeries(ThirdEyeResultSet resultSet, int keyIndex) {
    int rowCount = resultSet.getRowCount();
    if (rowCount <= 0) {
      return StringSeries.empty();
    }

    String[] values = new String[rowCount];
    for (int i = 0; i < rowCount; i++) {
      values[i] = resultSet.getGroupKeyColumnValue(i, keyIndex);
    }

    return DataFrame.toSeries(values);
  }
}
