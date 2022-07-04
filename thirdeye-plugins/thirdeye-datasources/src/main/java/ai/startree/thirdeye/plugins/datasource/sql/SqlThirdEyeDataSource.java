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
package ai.startree.thirdeye.plugins.datasource.sql;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.DataSourceUtils;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlThirdEyeDataSource implements ThirdEyeDataSource {

  private static final Logger LOG = LoggerFactory.getLogger(SqlThirdEyeDataSource.class);

  private LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> sqlResponseCache;
  private MetricConfigManager metricConfigManager;
  private SqlResponseCacheLoader sqlResponseCacheLoader;
  private String name;

  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    Map<String, Object> properties = context.getDataSourceDTO().getProperties();

    metricConfigManager = context.getMetricConfigManager();
    final DatasetConfigManager datasetConfigManager = context.getDatasetConfigManager();
    sqlResponseCacheLoader = new SqlResponseCacheLoader(properties,
        metricConfigManager,
        datasetConfigManager);
    sqlResponseCache = DataSourceUtils.buildResponseCache(sqlResponseCacheLoader);
    name = MapUtils.getString(properties, "name", SqlThirdEyeDataSource.class.getSimpleName());
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the cached ResultSetGroup corresponding to the given Presto query.
   *
   * @param SQLQuery the query that is specifically constructed for Presto.
   * @return the corresponding ThirdEyeResultSet to the given Presto query.
   */
  private ThirdEyeResultSetGroup executeSQL(SqlQuery SQLQuery) throws Exception {
    ThirdEyeResultSetGroup thirdEyeResultSetGroup;
    try {
      thirdEyeResultSetGroup = sqlResponseCache.get(SQLQuery);
    } catch (Exception e) {
      throw e;
    }
    return thirdEyeResultSetGroup;
  }

  @Override
  public List<String> getDatasets() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() throws Exception {
    // left blank
  }

  @Override
  public void close() throws Exception {
    // left blank
  }

  @Override
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return sqlResponseCacheLoader.getMaxDataTime(datasetConfig);
  }
}
