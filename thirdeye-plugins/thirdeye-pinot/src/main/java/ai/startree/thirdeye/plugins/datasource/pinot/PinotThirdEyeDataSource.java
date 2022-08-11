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
import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceUtils.getBetweenClause;
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
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotThirdEyeDataSource implements ThirdEyeDataSource {

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSource.class);
  private static final String TIME_QUERY_TEMPLATE = "SELECT %s(%s) FROM %s WHERE %s";

  private final SqlExpressionBuilder sqlExpressionBuilder;
  private final SqlLanguage sqlLanguage;
  private String name;
  private PinotResponseCacheLoader pinotResponseCacheLoader;
  private LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> pinotResponseCache;
  private ThirdEyeDataSourceContext context;

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

    try {
      pinotResponseCacheLoader = new PinotResponseCacheLoader(buildConfig(properties));
      pinotResponseCacheLoader.initConnections();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
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

  /**
   * Refreshes and returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  public ThirdEyeResultSetGroup refreshSQL(final PinotQuery pinotQuery) throws ExecutionException {
    requireNonNull(pinotResponseCache,
        String.format("%s doesn't connect to Pinot or cache is not initialized.", getName()));

    try {
      pinotResponseCache.refresh(pinotQuery);
      return pinotResponseCache.get(pinotQuery);
    } catch (final ExecutionException e) {
      LOG.error("Failed to refresh PQL: {}", pinotQuery.getQuery());
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
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    long maxTime = queryTimeSpecFromPinot("max", datasetConfig);
    if (maxTime <= 0) {
      maxTime = System.currentTimeMillis();
    }
    return maxTime;
  }

  /**
   * Returns the earliest time in millis for a dataset in pinot
   *
   * @return min (earliest) date time in millis. Returns 0 if dataset is not found
   */
  @Override
  public long getMinDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return queryTimeSpecFromPinot("min", datasetConfig);
  }

  private long queryTimeSpecFromPinot(final String functionName,
      final DatasetConfigDTO datasetConfig) {
    long maxTime = 0;
    final String dataset = datasetConfig.getDataset();
    try {
      // By default, query only offline, unless dataset has been marked as realtime
      final TimeSpec timeSpec = DataSourceUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);

      final long cutoffTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
      final String timeClause = getBetweenClause(new DateTime(0, DateTimeZone.UTC),
          new DateTime(cutoffTime, DateTimeZone.UTC),
          timeSpec,
          datasetConfig);

      final String maxTimePql = String
          .format(TIME_QUERY_TEMPLATE, functionName, timeSpec.getColumnName(), dataset, timeClause);
      final PinotQuery maxTimePinotQuery = new PinotQuery(maxTimePql, dataset);

      final ThirdEyeResultSetGroup resultSetGroup;
      try {
        refreshSQL(maxTimePinotQuery);
        resultSetGroup = executeSQL(maxTimePinotQuery);
      } catch (final ExecutionException e) {
        throw e;
      }

      if (resultSetGroup.size() == 0 || resultSetGroup.get(0).getRowCount() == 0) {
        LOG.error("Failed to get latest max time for dataset {} with SQL: {}", dataset,
            maxTimePinotQuery.getQuery());
      } else {
        final DateTimeZone timeZone = SpiUtils.getDateTimeZone(datasetConfig);

        final long endTime = resultSetGroup.get(0).getDouble(0).longValue();
        // endTime + 1 to make sure we cover the time range of that time value.
        final String timeFormat = timeSpec.getFormat();
        if (StringUtils.isBlank(timeFormat) || TimeSpec.SINCE_EPOCH_FORMAT.equals(timeFormat)) {
          maxTime = timeSpec.getDataGranularity().toMillis(endTime + 1, timeZone) - 1;
        } else {
          final DateTimeFormatter inputDataDateTimeFormatter =
              DateTimeFormat.forPattern(timeFormat).withZone(timeZone);
          final DateTime endDateTime = DateTime
              .parse(String.valueOf(endTime), inputDataDateTimeFormatter);
          final Period oneBucket = datasetConfig.bucketTimeGranularity().toPeriod();
          maxTime = endDateTime.plus(oneBucket).getMillis() - 1;
        }
      }
    } catch (final Exception e) {
      LOG.warn("Exception getting maxTime from collection: {}", dataset, e);
    }
    return maxTime;
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
    if (pinotResponseCacheLoader != null) {
      pinotResponseCacheLoader.close();
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
