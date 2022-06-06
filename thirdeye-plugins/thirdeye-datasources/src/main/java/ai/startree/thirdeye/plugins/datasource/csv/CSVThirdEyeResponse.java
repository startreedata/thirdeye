/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.csv;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.BaseThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * The response of ThirdEye if the data source is a CSV file.
 * Used by {@link CSVThirdEyeDataSource}
 */
public class CSVThirdEyeResponse extends BaseThirdEyeResponse {

  private static final String COL_TIMESTAMP = CSVThirdEyeDataSource.COL_TIMESTAMP;
  /**
   * The Dataframe.
   */
  DataFrame dataframe;

  /**
   * Instantiates a new Csv third eye response.
   *
   * @param request the ThirdEye request
   * @param dataTimeSpec the data time spec
   * @param df the data frame
   */
  public CSVThirdEyeResponse(ThirdEyeRequest request, TimeSpec dataTimeSpec, DataFrame df) {
    super(request, dataTimeSpec);
    this.dataframe = df;
  }

  /**
   * Get the number of rows in the data frame.
   *
   * @return the number of rows in the data frame
   */
  @Override
  public int getNumRows() {
    return dataframe.size();
  }

  /**
   * Get a row from the data frame.
   *
   * @param rowId row number
   * @return a ThirdEyeResponseRow
   */
  @Override
  public ThirdEyeResponseRow getRow(int rowId) {
    if (rowId >= dataframe.size()) {
      throw new IllegalArgumentException();
    }
    int timeBucketId = -1;

    if (dataframe.contains(COL_TIMESTAMP)) {
      long time = dataframe.getLong(COL_TIMESTAMP, rowId);
      timeBucketId = SpiUtils.computeBucketIndex(
          dataTimeSpec.getDataGranularity(),
          request.getStartTimeInclusive(),
          new DateTime(time, DateTimeZone.UTC));
    }

    List<String> dimensions = new ArrayList<>();
    for (String dimension : request.getGroupBy()) {
      dimensions.add(dataframe.getString(dimension, rowId));
    }

    List<Double> metrics = List.of(
        dataframe.getDouble(request.getMetricFunction().toString(), rowId)
    );
    return new ThirdEyeResponseRow(timeBucketId, dimensions, metrics);
  }

  /**
   * Get the number of rows for a metric function.
   *
   * @param metricFunction a MetricFunction
   * @return the number of rows for this metric function
   */
  @Override
  public int getNumRowsFor(MetricFunction metricFunction) {
    return dataframe.size();
  }

  /**
   * Get the row that corresponds to a metric function.
   *
   * @param metricFunction a MetricFunction
   * @return the row that corresponds to a metric function
   */
  @Override
  public Map<String, String> getRow(MetricFunction metricFunction, int rowId) {
    Map<String, String> rowMap = new HashMap<>();
    for (int i = 0; i < groupKeyColumns.size(); i++) {
      String dimension = groupKeyColumns.get(i);
      rowMap.put(dimension, dataframe.getString(dimension, rowId));
    }
    rowMap.put(metricFunction.toString(), dataframe.getString(metricFunction.toString(), rowId));
    return rowMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CSVThirdEyeResponse response = (CSVThirdEyeResponse) o;
    return Objects.equals(dataframe, response.dataframe);
  }

  @Override
  public int hashCode() {

    return Objects.hash(dataframe);
  }
}
