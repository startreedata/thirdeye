package org.apache.pinot.thirdeye.detection.v2.components.datafetcher;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import org.apache.pinot.thirdeye.detection.v2.spec.DataFetcherSpec;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.detection.DataFetcher;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;

public class GenericDataFetcher implements DataFetcher<DataFetcherSpec> {

  /**
   * Query to execute.
   */
  private String query;
  /**
   * Table to query.
   */
  private String tableName;

  private ThirdEyeDataSource thirdEyeDataSource;

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
      final String dataSource = requireNonNull(dataFetcherSpec.getDataSource(),
          "DataFetcher: data source is not set.");
      this.thirdEyeDataSource = requireNonNull(dataFetcherSpec
          .getDataSourceCache()
          .getDataSource(dataSource), "data source is unavailable");
    }
  }

  @Override
  public DataTable getDataTable() throws Exception {
    return thirdEyeDataSource.fetchDataTable(new ThirdEyeRequestV2(tableName,
        query,
        ImmutableMap.of()));
  }
}
