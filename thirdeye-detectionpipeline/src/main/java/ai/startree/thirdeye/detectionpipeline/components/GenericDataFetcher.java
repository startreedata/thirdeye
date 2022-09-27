/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline.components;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.detectionpipeline.sql.filter.FilterEngine;
import ai.startree.thirdeye.detectionpipeline.sql.macro.MacroEngine;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.metric.DimensionType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
  private DatasetConfigDTO datasetConfigDTO;
  private List<QueryPredicate> timeseriesFilters = List.of();

  public String getQuery() {
    return query;
  }

  public GenericDataFetcher setQuery(final String query) {
    this.query = query;
    return this;
  }

  @VisibleForTesting
  public String getTableName() {
    return tableName;
  }

  @VisibleForTesting
  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  @Override
  public void init(final DataFetcherSpec dataFetcherSpec) {
    this.query = dataFetcherSpec.getQuery();
    this.tableName = dataFetcherSpec.getTableName();
    if (tableName != null) {
      final DatasetConfigManager datasetDao = Objects.requireNonNull(dataFetcherSpec.getDatasetDao());
      this.datasetConfigDTO = Objects.requireNonNull(datasetDao.findByDataset(dataFetcherSpec.getTableName()),
          "Could not find dataset " + dataFetcherSpec.getTableName());
    }
    if (dataFetcherSpec.getDataSourceCache() != null) {
      final String dataSource = requireNonNull(dataFetcherSpec.getDataSource(),
          "DataFetcher: data source is not set.");
      this.thirdEyeDataSource = requireNonNull(dataFetcherSpec
          .getDataSourceCache()
          .getDataSource(dataSource), "data source is unavailable");
    }

    if (!dataFetcherSpec.getTimeseriesFilters().isEmpty()) {
      checkArgument(tableName != null,
          "tableName is not set in DataFetcherSpec. Cannot inject filters without tableName");
      this.timeseriesFilters = dataFetcherSpec.getTimeseriesFilters()
          .stream()
          .map(this::toQueryPredicate)
          .collect(Collectors.toList());
    }
  }

  @Override
  public DataTable getDataTable(Interval detectionInterval) throws Exception {
    String queryWithFilters = injectFilters(query);
    DataSourceRequest preparedRequest = applyMacros(detectionInterval, queryWithFilters);
    DataTable result = thirdEyeDataSource.fetchDataTable(preparedRequest);
    result.addProperties(preparedRequest.getProperties());
    return result;
  }

  private String injectFilters(final String query) {
    if (timeseriesFilters.isEmpty()) {
      return query;
    }
    SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();
    checkArgument(sqlLanguage != null,
        "Sql manipulation not supported for datasource %s, but filters list is not empty. Cannot apply filters.",
        thirdEyeDataSource.getName());
    return new FilterEngine(sqlLanguage, query, timeseriesFilters).prepareQuery();
  }

  private DataSourceRequest applyMacros(final Interval detectionInterval,
      final String queryWithFilters) {
    SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();
    SqlExpressionBuilder sqlExpressionBuilder = thirdEyeDataSource.getSqlExpressionBuilder();
    boolean macrosSupported = sqlLanguage != null && sqlExpressionBuilder != null;
    if (macrosSupported) {
      return new MacroEngine(sqlLanguage,
          sqlExpressionBuilder,
          detectionInterval,
          datasetConfigDTO,
          queryWithFilters).prepareRequest();
    }
    return new DataSourceRequest(tableName, query, ImmutableMap.of());
  }

  @VisibleForTesting
  protected QueryPredicate toQueryPredicate(final Predicate p) {
    // pre-condition: tableName is not null
    final DimensionType dimensionType = getDimensionType(p.getLhs(), tableName);
    return QueryPredicate.of(p, dimensionType, tableName);
  }

  // fixme datatype from metricDTO is always string + abstraction metric/dimension needs refactoring
  private DimensionType getDimensionType(final String metric, final String dataset) {
    // first version: assume dimension is always of type String
    // todo fetch info from database with a DAO
    return DimensionType.STRING;
  }
}
