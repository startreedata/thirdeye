/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.detectionpipeline.spec;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
   * The table to query.
   *
   * Optional - only necessary for AUTO mode of macros and better broker choice in pinot client.
   */
  private String tableName;
  /**
   * Expected to be set during DataFetcherOperator init
   */
  private DataSourceCache dataSourceCache;
  /**
   * Expected to be set during DataFetcherOperator init
   * */
  private DatasetConfigManager datasetDao;

  /**
   * Expected to be set during DataFetcherOperator init
   * */
  private DataSourceManager dataSourceDao;

  /**
   * Expected to be set during DataFetcherOperator init.
   */
  private List<Predicate> timeseriesFilters;

  /**
   * Expected to be set during DataFetcherOperator init.
   */
  private String namespace;

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

  public DatasetConfigManager getDatasetDao() {
    return datasetDao;
  }

  public DataFetcherSpec setDatasetDao(
      final DatasetConfigManager datasetDao) {
    this.datasetDao = datasetDao;
    return this;
  }

  public List<Predicate> getTimeseriesFilters() {
    return timeseriesFilters;
  }

  public DataFetcherSpec setTimeseriesFilters(
      final List<Predicate> timeseriesFilters) {
    this.timeseriesFilters = timeseriesFilters;
    return this;
  }

  public String getNamespace() {
    return namespace;
  }

  public DataFetcherSpec setNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  public DataSourceManager getDataSourceDao() {
    return dataSourceDao;
  }

  public DataFetcherSpec setDataSourceDao(
      final DataSourceManager dataSourceDao) {
    this.dataSourceDao = dataSourceDao;
    return this;
  }
}
