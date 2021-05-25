package org.apache.pinot.thirdeye.detection.v2.components.datafetcher;

import java.util.concurrent.ExecutionException;
import org.apache.pinot.thirdeye.datasource.pinot.PinotQuery;
import org.apache.pinot.thirdeye.datasource.pinot.PinotThirdEyeDataSource;
import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.detection.v2.results.DataTable;
import org.apache.pinot.thirdeye.detection.v2.spec.DataFetcherSpec;

public class PinotDataFetcher implements DataFetcher<DataFetcherSpec> {

  /**
   * Query to execute.
   */
  private String query;
  /**
   * Table to query.
   */
  private String tableName;

  private PinotThirdEyeDataSource pinotDataSource;

  public String getQuery() {
    return query;
  }

  public PinotDataFetcher setQuery(final String query) {
    this.query = query;
    return this;
  }

  public String getTableName() {
    return tableName;
  }

  public PinotDataFetcher setTableName(final String tableName) {
    this.tableName = tableName;
    return this;
  }

  @Override
  public void init(final DataFetcherSpec dataFetcherSpec) {
    this.query = dataFetcherSpec.getQuery();
    this.tableName = dataFetcherSpec.getTableName();
    if (dataFetcherSpec.getDataSourceCache() != null) {
      this.pinotDataSource = (PinotThirdEyeDataSource) dataFetcherSpec.getDataSourceCache()
          .getDataSource(dataFetcherSpec.getDataSource());
    }
  }

  @Override
  public DataTable getDataTable() throws Exception {
    try {
      ThirdEyeResultSetGroup thirdEyeResultSetGroup = pinotDataSource.executeSQL(new PinotQuery(
          query,
          tableName));
      return new DataTable(thirdEyeResultSetGroup);
    } catch (ExecutionException e) {
      throw e;
    }
  }
}
