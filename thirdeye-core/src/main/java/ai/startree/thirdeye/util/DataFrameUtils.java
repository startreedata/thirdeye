/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.joda.time.DateTime;
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
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database. Also aligns start and end timestamps by
   * rounding them down (start) and up (end) to align with metric time granularity boundaries.
   * <br/><b>NOTE:</b> the aligned end timestamp is still exclusive.
   *
   * @param slice metric data slice
   * @param reference unique identifier for request
   * @param datasetDAO dataset config DAO
   * @return TimeSeriesRequestContainer
   */
  public static TimeSeriesRequestContainer makeTimeSeriesRequestAligned(MetricSlice slice,
      String reference, DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    MetricConfigDTO metricConfigDTO = slice.getMetricConfigDTO();

    DatasetConfigDTO dataset = datasetDAO.findByDataset(metricConfigDTO.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(String
          .format("Could not resolve dataset '%s' for metric id '%d'", metricConfigDTO.getDataset(),
              metricConfigDTO.getId()));
    }

    List<MetricExpression> expressions = Utils.convertToMetricExpressions(metricConfigDTO.getName(),
        metricConfigDTO.getDefaultAggFunction(), metricConfigDTO.getDataset(),
        thirdEyeCacheRegistry);

    TimeGranularity granularity = Optional.ofNullable(slice.getGranularity())
        .orElse(dataset.bucketTimeGranularity());

    Period period = granularity.toPeriod();

    DateTime start = slice.getStart().withFields(SpiUtils.makeOrigin(period.getPeriodType()));
    DateTime end = slice.getEnd().withFields(SpiUtils.makeOrigin(period.getPeriodType()));

    MetricSlice alignedSlice = MetricSlice
        .from(slice.getMetricId(),
            start.getMillis(),
            end.getMillis(),
            slice.getFilters(),
            slice.getGranularity());

    ThirdEyeRequest request = makeThirdEyeRequestBuilder(alignedSlice, dataset, expressions,
        thirdEyeCacheRegistry
    )
        .setGroupByTimeGranularity(granularity)
        .build(reference);

    return new TimeSeriesRequestContainer(request, expressions, start, end, granularity.toPeriod());
  }

  /**
   * Constructs and wraps a request for a simple metric.
   *
   * Assumes the slice contains a complete metricConfigDTO.
   *
   * @param slice metric data slice
   * @param dimensions dimensions to group by
   * @param limit top k element limit ({@code -1} for default)
   * @param reference unique identifier for request
   * @return RequestContainer
   */
  public static RequestContainer makeAggregateRequest(MetricSlice slice,
      List<String> dimensions,
      int limit,
      String reference,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    MetricConfigDTO metricConfigDTO = Objects.requireNonNull(slice.getMetricConfigDTO());

    MetricExpression expression = new MetricExpression(
        metricConfigDTO.getName(),
        Optional.ofNullable(metricConfigDTO.getDerivedMetricExpression()).orElse(metricConfigDTO.getName()),
        metricConfigDTO.getDefaultAggFunction(),
        metricConfigDTO.getDataset());

    ThirdEyeRequest request = makeThirdEyeRequestBuilder(slice, List.of(expression),
        thirdEyeCacheRegistry
    )
        .setGroupBy(dimensions)
        .setLimit(limit)
        .build(reference);

    return new RequestContainer(request, List.of(expression));
  }

  @Deprecated
  // use above - do not pass DAOs to dataframe utils
  public static RequestContainer makeAggregateRequest(MetricSlice slice,
      List<String> dimensions,
      int limit,
      String reference,
      MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    MetricConfigDTO metricConfigDTO = metricDAO.findById(slice.getMetricId());
    if (metricConfigDTO == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric '%s'", slice.getMetricId()));
    }

    DatasetConfigDTO dataset = datasetDAO.findByDataset(metricConfigDTO.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(String
          .format("Could not resolve dataset '%s' for metric id '%d'", metricConfigDTO.getDataset(),
              metricConfigDTO.getId()));
    }

    return makeAggregateRequest(slice.withMetricConfigDto(metricConfigDTO)
            .withDatasetConfigDto(dataset),
        dimensions,
        limit,
        reference,
        thirdEyeCacheRegistry);
  }

  /**
   * Helper: Returns a pre-populated ThirdeyeRequestBuilder instance. Removes invalid filter values.
   *
   * @param slice metric data slice
   * @param dataset dataset dto
   * @param expressions metric expressions
   * @return ThirdeyeRequestBuilder
   */
  @Deprecated
  private static ThirdEyeRequest.ThirdEyeRequestBuilder makeThirdEyeRequestBuilder(
      MetricSlice slice,
      DatasetConfigDTO dataset,
      List<MetricExpression> expressions,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    DatasetConfigDTO datasetConfigDTO = slice.getDatasetConfigDTO();
    datasetConfigDTO.setDataSource(dataset.getDataSource());

    return makeThirdEyeRequestBuilder(slice.withDatasetConfigDto(datasetConfigDTO),
        expressions, thirdEyeCacheRegistry);
  }

  private static ThirdEyeRequest.ThirdEyeRequestBuilder makeThirdEyeRequestBuilder(
      MetricSlice slice,
      List<MetricExpression> expressions,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    List<MetricFunction> functions = new ArrayList<>();
    for (MetricExpression exp : expressions) {
      functions.addAll(exp.computeMetricFunctions(
          thirdEyeCacheRegistry));
    }

    return ThirdEyeRequest.newBuilder()
        .setStartTimeInclusive(slice.getStart())
        .setEndTimeExclusive(slice.getEnd())
        .setFilterSet(slice.getFilters())
        .setMetricFunctions(functions)
        .setDataSource(slice.getDatasetConfigDTO().getDataSource());
  }
}
