/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.spec;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.v2.TimeseriesFilter;
import java.util.List;

public class DataFetcherSpec extends AbstractSpec {

  /**
   * The name refer to ThirdEyeDataSource
   */
  private String dataSource;
  /**
   * The query to execute
   */
  private String query;
  /**
   * The table to query
   */
  private String tableName;
  /**
   * Expected to be set during DataFetcherOperator init
   */
  private DataSourceCache dataSourceCache;

  /**
   * Expected to be set during DataFetcherOperator init.
   */
  private List<TimeseriesFilter> timeseriesFilters;

  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(final String dataSource) {
    this.dataSource = dataSource;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(final String query) {
    this.query = query;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  public DataSourceCache getDataSourceCache() {
    return dataSourceCache;
  }

  public DataFetcherSpec setDataSourceCache(
      final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    return this;
  }

  public List<TimeseriesFilter> getTimeseriesFilters() {
    return timeseriesFilters;
  }

  public DataFetcherSpec setTimeseriesFilters(
      final List<TimeseriesFilter> timeseriesFilters) {
    this.timeseriesFilters = timeseriesFilters;
    return this;
  }
}
