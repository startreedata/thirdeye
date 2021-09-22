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

package org.apache.pinot.thirdeye.datasource.pinotsql;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.pinot.thirdeye.datasource.auto.onboard.PinotDatasetOnboarder;
import org.apache.pinot.thirdeye.datasource.auto.onboard.ThirdEyePinotClient;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.RelationalQuery;
import org.apache.pinot.thirdeye.spi.datasource.RelationalThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlThirdEyeDataSource implements ThirdEyeDataSource {

  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlThirdEyeDataSource.class);

  private String name;
  private PinotSqlResponseCacheLoader pinotSqlResponseCacheLoader;
  private LoadingCache<RelationalQuery, ResultSet> pinotResponseCache;
  private ThirdEyeDataSourceContext context;


  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    this.context = context;

    final DataSourceDTO dataSourceDTO = requireNonNull(context.getDataSourceDTO(),
        "data source dto is null");

    final Map<String, Object> properties = requireNonNull(dataSourceDTO.getProperties(),
        "Data source property cannot be empty.");
    name = requireNonNull(dataSourceDTO.getName(), "name of data source dto is null");

    try {
      pinotSqlResponseCacheLoader = new PinotSqlControllerResponseCacheLoader();
      pinotSqlResponseCacheLoader.init(properties);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    pinotResponseCache = SqlUtils.buildResponseCache(pinotSqlResponseCacheLoader);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public RelationalThirdEyeResponse execute(ThirdEyeRequest request) throws Exception {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotSqlQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  public ResultSet executeSQL(PinotSqlQuery pinotSqlQuery) throws ExecutionException {
    Preconditions
        .checkNotNull(this.pinotResponseCache,
            "{} doesn't connect to Pinot or cache is not initialized.", getName());

    try {
      return this.pinotResponseCache.get(pinotSqlQuery);
    } catch (ExecutionException e) {
      LOG.error("Failed to execute SQL: {}", pinotSqlQuery.getQuery());
      throw e;
    }
  }

  /**
   * Refreshes and returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotSqlQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  public ResultSet refreshSQL(PinotSqlQuery pinotSqlQuery) throws ExecutionException {
    requireNonNull(this.pinotResponseCache,
        String.format("%s doesn't connect to Pinot or cache is not initialized.", getName()));

    try {
      pinotResponseCache.refresh(pinotSqlQuery);
      return pinotResponseCache.get(pinotSqlQuery);
    } catch (ExecutionException e) {
      LOG.error("Failed to refresh PQL: {}", pinotSqlQuery.getQuery());
      throw e;
    }
  }

  @Override
  public List<String> getDatasets() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataTable fetchDataTable(final ThirdEyeRequestV2 request) throws Exception {
    ResultSet resultSet = executeSQL(new PinotSqlQuery(
        request.getQuery(),
        request.getTable()));
    return new PinotSqlDataTable(resultSet);
  }

  @Override
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getMinDataTime(final DatasetConfigDTO datasetConfig) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, List<String>> getDimensionFilters(final DatasetConfigDTO datasetConfig)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean validate() {
    try {
      String query = "select 1 from x";
      ResultSet result = executeSQL(new PinotSqlQuery(query, "x"));
      return result.getLong(1) == 1;
    } catch (Exception e) {
      LOG.error("Exception while performing pinot datasource validation.", e);
    }
    return false;
  }

  @Override
  public void close() throws Exception {
    if (pinotSqlResponseCacheLoader != null) {
      pinotSqlResponseCacheLoader.close();
    }
  }

  @Override
  public List<DatasetConfigDTO> onboardAll() {
    final PinotDatasetOnboarder pinotDatasetOnboarder = createPinotDatasetOnboarder();

    try {
      return pinotDatasetOnboarder.onboardAll(name);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DatasetConfigDTO onboardDataset(final String datasetName) {
    final PinotDatasetOnboarder pinotDatasetOnboarder = createPinotDatasetOnboarder();

    try {
      return pinotDatasetOnboarder.onboardTable(datasetName, name);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private PinotDatasetOnboarder createPinotDatasetOnboarder() {
    final ThirdEyePinotClient thirdEyePinotSqlClient = new ThirdEyePinotClient(new DataSourceMetaBean()
        .setProperties(context.getDataSourceDTO().getProperties()), "pinot-sql");
    return new PinotDatasetOnboarder(
        thirdEyePinotSqlClient,
        context.getDatasetConfigManager(),
        context.getMetricConfigManager());
  }
}
