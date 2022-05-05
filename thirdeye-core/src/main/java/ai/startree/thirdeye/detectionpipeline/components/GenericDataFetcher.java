/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.components;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.detectionpipeline.sql.filter.FiltersEngine;
import ai.startree.thirdeye.detectionpipeline.sql.macro.MacroEngine;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequestV2;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.apache.calcite.sql.parser.SqlParseException;
import org.joda.time.Interval;

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
  private List<QueryPredicate> timeseriesFilters;

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
    this.timeseriesFilters = dataFetcherSpec.getTimeseriesFilters();
  }

  @Override
  public DataTable getDataTable(Interval detectionInterval) throws Exception {
    String queryWithFilters = injectFilters(query);
    ThirdEyeRequestV2 preparedRequest = applyMacros(detectionInterval, queryWithFilters);
    DataTable result = thirdEyeDataSource.fetchDataTable(preparedRequest);
    result.addProperties(preparedRequest.getProperties());
    return result;
  }

  private String injectFilters(final String query) throws SqlParseException {
    if (timeseriesFilters.isEmpty()) {
      return query;
    }
    SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();
    checkArgument(sqlLanguage != null,
        String.format(
            "Sql manipulation not supported for datasource %s, but filters list is not empty. Cannot apply filters.",
            thirdEyeDataSource.getName()));
    return new FiltersEngine(sqlLanguage, query, timeseriesFilters).prepareQuery();
  }

  private ThirdEyeRequestV2 applyMacros(final Interval detectionInterval,
      final String queryWithFilters)
      throws SqlParseException {
    SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();
    SqlExpressionBuilder sqlExpressionBuilder = thirdEyeDataSource.getSqlExpressionBuilder();
    boolean macrosSupported = sqlLanguage != null && sqlExpressionBuilder != null;
    if (macrosSupported) {
      return new MacroEngine(sqlLanguage,
          sqlExpressionBuilder,
          detectionInterval,
          tableName,
          queryWithFilters).prepareRequest();
    }
    return new ThirdEyeRequestV2(tableName, query, ImmutableMap.of());
  }
}
