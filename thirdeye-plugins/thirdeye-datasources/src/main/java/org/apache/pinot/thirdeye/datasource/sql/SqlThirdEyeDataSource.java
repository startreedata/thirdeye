/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datasource.sql;

import com.google.common.cache.LoadingCache;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.DataSourceUtils;
import org.apache.pinot.thirdeye.spi.datasource.MetricFunction;
import org.apache.pinot.thirdeye.spi.datasource.RelationalQuery;
import org.apache.pinot.thirdeye.spi.datasource.RelationalThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetUtils;
import org.apache.pinot.thirdeye.spi.detection.TimeSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
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
  public ThirdEyeResponse execute(ThirdEyeRequest request) throws Exception {
    LinkedHashMap<MetricFunction, List<ThirdEyeResultSet>> metricFunctionToResultSetList = new LinkedHashMap<>();
    TimeSpec timeSpec = null;
    String sourceName = "";
    try {
      for (MetricFunction metricFunction : request.getMetricFunctions()) {
        String dataset = metricFunction.getDataset();
        DatasetConfigDTO datasetConfig = metricFunction.getDatasetConfig();
        TimeSpec dataTimeSpec = DataSourceUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);

        if (timeSpec == null) {
          timeSpec = dataTimeSpec;
        }

        String[] tableComponents = dataset.split("\\.");
        sourceName = tableComponents[0];
        String dbName = tableComponents[1];

        String sqlQuery = SqlUtils
            .getSql(request, metricFunction, request.getFilterSet(), dataTimeSpec, sourceName,
                metricConfigManager);
        ThirdEyeResultSetGroup thirdEyeResultSetGroup = executeSQL(
            new SqlQuery(sqlQuery,
                sourceName,
                dbName,
                metricFunction.getMetricName(),
                request.getGroupBy(),
                request.getGroupByTimeGranularity(),
                dataTimeSpec));

        metricFunctionToResultSetList.put(metricFunction, thirdEyeResultSetGroup.getResultSets());
      }
      List<String[]> resultRows = ThirdEyeResultSetUtils
          .parseResultSets(request, metricFunctionToResultSetList,
              sourceName);

      return new RelationalThirdEyeResponse(request, resultRows, timeSpec);
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public DataTable fetchDataTable(final ThirdEyeRequestV2 request) throws Exception {
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

  @Override
  public Map<String, List<String>> getDimensionFilters(final DatasetConfigDTO datasetConfig)
      throws Exception {
    return this.sqlResponseCacheLoader.getDimensionFilters(datasetConfig);
  }
}
