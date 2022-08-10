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
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
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
  private ThirdEyeDataSource thirdEyeDataSource;
  private DatasetConfigDTO datasetConfigDTO;
  private List<QueryPredicate> timeseriesFilters;

  public String getQuery() {
    return query;
  }

  public GenericDataFetcher setQuery(final String query) {
    this.query = query;
    return this;
  }

  @VisibleForTesting
  public String getTableName() {
    return datasetConfigDTO.getDataset();
  }

  @Override
  public void init(final DataFetcherSpec dataFetcherSpec) {
    this.query = dataFetcherSpec.getQuery();
    final DatasetConfigManager datasetDao = Objects.requireNonNull(dataFetcherSpec.getDatasetDao());
    this.datasetConfigDTO = Objects.requireNonNull(datasetDao.findByDataset(dataFetcherSpec.getTableName()),
        "Could not find dataset " + dataFetcherSpec.getTableName());
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
    DataSourceRequest preparedRequest = applyMacros(detectionInterval, queryWithFilters);
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
    return new FilterEngine(sqlLanguage, query, timeseriesFilters).prepareQuery();
  }

  private DataSourceRequest applyMacros(final Interval detectionInterval,
      final String queryWithFilters)
      throws SqlParseException {
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
    return new DataSourceRequest(datasetConfigDTO.getDataset(), query, ImmutableMap.of());
  }
}
