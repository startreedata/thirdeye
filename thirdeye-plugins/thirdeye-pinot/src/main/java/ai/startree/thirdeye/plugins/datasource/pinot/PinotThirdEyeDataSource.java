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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceUtils.buildConfig;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.auto.onboard.PinotDatasetOnboarder;
import ai.startree.thirdeye.plugins.datasource.auto.onboard.ThirdEyePinotClient;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.DataSourceUtils;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.pinot.client.PinotConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotThirdEyeDataSource implements ThirdEyeDataSource {

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSource.class);

  private final SqlExpressionBuilder sqlExpressionBuilder;
  private final SqlLanguage sqlLanguage;
  private String name;
  private LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> pinotResponseCache;
  private ThirdEyeDataSourceContext context;
  private PinotConnectionManager pinotConnectionManager;

  public PinotThirdEyeDataSource(
      final SqlExpressionBuilder sqlExpressionBuilder,
      final PinotSqlLanguage sqlLanguage) {
    this.sqlExpressionBuilder = sqlExpressionBuilder;
    this.sqlLanguage = sqlLanguage;
  }

  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    this.context = context;
    final DataSourceDTO dataSourceDTO = requireNonNull(context.getDataSourceDTO(),
        "data source dto is null");

    final Map<String, Object> properties = requireNonNull(dataSourceDTO.getProperties(),
        "Data source property cannot be empty.");
    name = requireNonNull(dataSourceDTO.getName(), "name of data source dto is null");

    /* Create config class */
    final var config = buildConfig(properties);

    /* Build Connection Manager */
    pinotConnectionManager = new PinotConnectionManager(new PinotConnectionBuilder(), config);

    /* Cache loader uses the connection to fire queries */
    final var pinotResponseCacheLoader = new PinotResponseCacheLoader(pinotConnectionManager);

    /* Uses LoadingCache to cache queries */
    pinotResponseCache = DataSourceUtils.buildResponseCache(pinotResponseCacheLoader);
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  public ThirdEyeResultSetGroup executeSQL(final PinotQuery pinotQuery) throws ExecutionException {
    Preconditions
        .checkNotNull(pinotResponseCache,
            "{} doesn't connect to Pinot or cache is not initialized.", getName());

    try {
      return pinotResponseCache.get(pinotQuery);
    } catch (final ExecutionException e) {
      LOG.error("Failed to execute PQL: {}", pinotQuery.getQuery());
      throw e;
    }
  }

  @Override
  public List<String> getDatasets() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    try {
      // Use pinot SQL.
      final ThirdEyeResultSet thirdEyeResultSet = executeSQL(new PinotQuery(
          request.getQuery(),
          request.getTable(),
          true)).get(0);
      return new ThirdEyeResultSetDataTable(thirdEyeResultSet);
    } catch (final ExecutionException e) {
      throw e;
    }
  }

  @Override
  public boolean validate() {
    try {
      // Table name required to execute query against pinot broker.
      final PinotDatasetOnboarder onboard = createPinotDatasetOnboarder();
      final String table = onboard.getAllTables().get(0);
      final String query = String.format("select 1 from %s", table);

      final PinotQuery pinotQuery = new PinotQuery(query, table, true);

      /* Disable caching for validate queries */
      pinotResponseCache.refresh(pinotQuery);
      final ThirdEyeResultSetGroup result = executeSQL(pinotQuery);
      return result.get(0).getRowCount() == 1;
    } catch (final ExecutionException | IOException | ArrayIndexOutOfBoundsException e) {
      LOG.error("Exception while performing pinot datasource validation.", e);
    }
    return false;
  }

  @Override
  public List<DatasetConfigDTO> onboardAll() {
    final PinotDatasetOnboarder pinotDatasetOnboarder = createPinotDatasetOnboarder();

    try {
      return pinotDatasetOnboarder.onboardAll(name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DatasetConfigDTO onboardDataset(final String datasetName) {
    final PinotDatasetOnboarder pinotDatasetOnboarder = createPinotDatasetOnboarder();

    try {
      return pinotDatasetOnboarder.onboardTable(datasetName, name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private PinotDatasetOnboarder createPinotDatasetOnboarder() {
    final ThirdEyePinotClient thirdEyePinotClient = new ThirdEyePinotClient(new DataSourceMetaBean()
        .setProperties(context.getDataSourceDTO().getProperties()), "pinot");
    return new PinotDatasetOnboarder(
        thirdEyePinotClient,
        context.getDatasetConfigManager(),
        context.getMetricConfigManager());
  }

  @Override
  public void close() throws Exception {
    if (pinotConnectionManager != null) {
      pinotConnectionManager.close();
    }
  }

  @Override
  public SqlLanguage getSqlLanguage() {
    return sqlLanguage;
  }

  @Override
  public SqlExpressionBuilder getSqlExpressionBuilder() {
    return sqlExpressionBuilder;
  }
}
