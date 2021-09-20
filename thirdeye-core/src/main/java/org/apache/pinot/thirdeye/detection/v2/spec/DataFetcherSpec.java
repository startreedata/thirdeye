package org.apache.pinot.thirdeye.detection.v2.spec;

import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;

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
}
