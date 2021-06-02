package org.apache.pinot.thirdeye.detection.v2.components.datafetcher;

import com.google.common.collect.ImmutableMap;
import org.apache.pinot.thirdeye.detection.v2.spec.DataFetcherSpec;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.components.DataFetcher;

public class GenericDataFetcher implements DataFetcher<DataFetcherSpec> {

  /**
   * Query to execute.
   */
  private String query;
  /**
   * Table to query.
   */
  private String tableName;

  private ThirdEyeDataSource pinotDataSource;

  public String getQuery() {
    return query;
  }

  public GenericDataFetcher setQuery(final String query) {
    this.query = query;
    return this;
  }

  public String getTableName() {
    return tableName;
  }

  public GenericDataFetcher setTableName(final String tableName) {
    this.tableName = tableName;
    return this;
  }

  @Override
  public void init(final DataFetcherSpec dataFetcherSpec) {
    this.query = dataFetcherSpec.getQuery();
    this.tableName = dataFetcherSpec.getTableName();
    if (dataFetcherSpec.getDataSourceCache() != null) {
      this.pinotDataSource = dataFetcherSpec.getDataSourceCache()
          .getDataSource(dataFetcherSpec.getDataSource());
    }
  }

  @Override
  public DataTable getDataTable() throws Exception {
    return pinotDataSource.fetchDataTable(new ThirdEyeRequestV2(tableName, query, ImmutableMap.of()));
  }
}
