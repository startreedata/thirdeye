/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.ArrayList;
import java.util.List;
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
   * Returns a Thirdeye response parsed as a DataFrame. The method stores the time values in
   * {@code COL_TIME} by default, and creates columns for each groupBy attribute and for each
   * MetricFunction specified in the request. It further evaluates expressions for derived
   * metrics.
   *
   * @param response thirdeye client response
   * @param metricFunction metricFunction
   * @return response as dataframe
   */
  public static DataFrame evaluateResponse(ThirdEyeResponse response, MetricFunction metricFunction) {
    // only the name is used to rename the result column --> inline this?
    DataFrame res = parseResponse(response);
    return res.renameSeries(metricFunction.toString(),DataFrame.COL_VALUE);
  }

  /**
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database. Also aligns start and end timestamps by
   * rounding them down (start) and up (end) to align with metric time granularity boundaries.
   * <br/><b>NOTE:</b> the aligned end timestamp is still exclusive.
   *
   * @param slice metric data slice
   * @param reference unique identifier for request
   * @return ThirdEyeRequest
   */
  public static ThirdEyeRequest makeTimeSeriesRequestAligned(MetricSlice slice, String reference) {
    MetricConfigDTO metricConfigDTO = slice.getMetricConfigDTO();
    DatasetConfigDTO datasetConfigDTO = slice.getDatasetConfigDTO();
    MetricFunction function = new MetricFunction(metricConfigDTO, datasetConfigDTO);

    TimeGranularity granularity = Optional.ofNullable(slice.getGranularity())
        .orElse(datasetConfigDTO.bucketTimeGranularity());

    Period period = granularity.toPeriod();
    DateTime start = slice.getStart().withFields(SpiUtils.makeOrigin(period.getPeriodType()));
    DateTime end = slice.getEnd().withFields(SpiUtils.makeOrigin(period.getPeriodType()));

    return ThirdEyeRequest.newBuilder()
        .setStartTimeInclusive(start)
        .setEndTimeExclusive(end)
        .setFilterSet(slice.getFilters())
        .setMetricFunction(function)
        .setDataSource(datasetConfigDTO.getDataSource())
        .setGroupByTimeGranularity(granularity)
        .build(reference);
  }

  /**
   * Constructs and wraps a request for a simple metric.
   *
   * Assumes the slice contains a complete metricConfigDTO.
   * Assumes the slice contains a complete datasetConfigDTO.
   *
   * @param slice metric data slice
   * @param dimensions dimensions to group by
   * @param limit top k element limit ({@code -1} for default)
   * @param reference unique identifier for request
   * @return ThirdEyeRequest
   */
  public static ThirdEyeRequest makeAggregateRequest(MetricSlice slice,
      List<String> dimensions,
      int limit,
      String reference) {
    MetricFunction function = new MetricFunction(slice.getMetricConfigDTO(), slice.getDatasetConfigDTO());
    return ThirdEyeRequest.newBuilder()
        .setStartTimeInclusive(slice.getStart())
        .setEndTimeExclusive(slice.getEnd())
        .setFilterSet(slice.getFilters())
        .setMetricFunction(function)
        .setDataSource(slice.getDatasetConfigDTO().getDataSource())
        .setGroupBy(dimensions)
        .setLimit(limit)
        .build(reference);
  }

  @Deprecated
  // use above - do not pass DAOs to dataframe utils
  public static ThirdEyeRequest makeAggregateRequest(MetricSlice slice,
      List<String> dimensions,
      int limit,
      String reference,
      MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO) {
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
        reference);
  }
}
