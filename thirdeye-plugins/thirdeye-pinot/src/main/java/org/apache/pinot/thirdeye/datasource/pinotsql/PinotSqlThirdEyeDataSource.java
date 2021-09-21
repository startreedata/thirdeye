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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.datasource.auto.onboard.PinotDatasetOnboarder;
import org.apache.pinot.thirdeye.datasource.auto.onboard.ThirdEyePinotClient;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.LogicalView;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.DataSourceUtils;
import org.apache.pinot.thirdeye.spi.datasource.MetricFunction;
import org.apache.pinot.thirdeye.spi.datasource.RelationalQuery;
import org.apache.pinot.thirdeye.spi.datasource.RelationalThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetDataTable;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetUtils;
import org.apache.pinot.thirdeye.spi.detection.TimeSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils;
import org.apache.pinot.thirdeye.spi.rootcause.util.FilterPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlThirdEyeDataSource implements ThirdEyeDataSource {

  private static final String EQUALS = "=";
  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlThirdEyeDataSource.class);
  private static final String PINOT_SQL = "PinotSQL";

  private String name;
  private PinotSqlResponseCacheLoader pinotSqlResponseCacheLoader;
  private LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> pinotResponseCache;
  private ThirdEyeDataSourceContext context;

  /**
   * Definition of Pre-Aggregated Data: the data that has been pre-aggregated or pre-calculated and
   * should not be
   * applied with any aggregation function during grouping by. Usually, this kind of data exists in
   * non-additive
   * dataset. For such data, we assume that there exists a dimension value named "all", which could
   * be overridden
   * in dataset configuration, that stores the pre-aggregated value.
   *
   * By default, when a query does not specify any value on pre-aggregated dimension, Pinot
   * aggregates all values
   * at that dimension, which is an undesirable behavior for non-additive data. Therefore, this
   * method modifies the
   * request's dimension filters such that the filter could pick out the "all" value for that
   * dimension. Example:
   * Suppose that we have a dataset with 3 pre-aggregated dimensions: country, pageName, and osName,
   * and the pre-
   * aggregated keyword is 'all'. Further assume that the original request's filter =
   * {'country'='US, IN'} and
   * GroupBy dimension = pageName, then the decorated request has the new filter =
   * {'country'='US, IN', 'osName' = 'all'}. Note that 'pageName' = 'all' is not in the filter set
   * because it is
   * a GroupBy dimension, which will not be aggregated.
   *
   * @param filterSet the original filterSet, which will NOT be modified.
   * @return a decorated filter set for the queries to the pre-aggregated dataset.
   */
  public static Multimap<String, String> generateFilterSetWithPreAggregatedDimensionValue(
      Multimap<String, String> filterSet, List<String> groupByDimensions,
      List<String> allDimensions,
      List<String> dimensionsHaveNoPreAggregation, String preAggregatedKeyword) {

    Set<String> preAggregatedDimensionNames = new HashSet<>(allDimensions);
    // Remove dimension names that do not have the pre-aggregated value
    if (CollectionUtils.isNotEmpty(dimensionsHaveNoPreAggregation)) {
      preAggregatedDimensionNames.removeAll(dimensionsHaveNoPreAggregation);
    }
    // Remove dimension names that have been included in the original filter set because we should not override
    // users' explicit filter setting
    if (filterSet != null) {
      preAggregatedDimensionNames.removeAll(filterSet.asMap().keySet());
    }
    // Remove dimension names that are going to be grouped by because GroupBy dimensions will not be aggregated anyway
    if (CollectionUtils.isNotEmpty(groupByDimensions)) {
      preAggregatedDimensionNames.removeAll(groupByDimensions);
    }
    // Add pre-aggregated dimension value to the remaining dimension names
    // exclude pre-aggregated dimension for group by dimensions
    Multimap<String, String> decoratedFilterSet;
    if (filterSet != null) {
      decoratedFilterSet = HashMultimap.create(filterSet);
    } else {
      decoratedFilterSet = HashMultimap.create();
    }
    if (preAggregatedDimensionNames.size() != 0) {
      for (String preComputedDimensionName : preAggregatedDimensionNames) {
        decoratedFilterSet.put(preComputedDimensionName, preAggregatedKeyword);
      }
      for (String dimensionName : groupByDimensions) {
        decoratedFilterSet.put(dimensionName, "!" + preAggregatedKeyword);
      }
    }

    return decoratedFilterSet;
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
      pinotSqlResponseCacheLoader = new PinotSqlControllerResponseCacheLoader();
      pinotSqlResponseCacheLoader.init(properties);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    pinotResponseCache = DataSourceUtils.buildResponseCache(pinotSqlResponseCacheLoader);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public RelationalThirdEyeResponse execute(ThirdEyeRequest request) throws Exception {
    Preconditions.checkNotNull(this.pinotResponseCache,
        "{} doesn't connect to Pinot or cache is not initialized.",
        getName());

//    long tStart = System.nanoTime();
    LinkedHashMap<MetricFunction, List<ThirdEyeResultSet>> metricFunctionToResultSetList = new LinkedHashMap<>();

    TimeSpec timeSpec = null;
    for (MetricFunction metricFunction : request.getMetricFunctions()) {
      String dataset = metricFunction.getDataset();
      DatasetConfigDTO datasetConfig = metricFunction.getDatasetConfig();
      TimeSpec dataTimeSpec = DataSourceUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);
      if (timeSpec == null) {
        timeSpec = dataTimeSpec;
      }

      MetricConfigDTO metricConfig = metricFunction.getMetricConfig();
      Multimap<String, String> filterSetFromView;
      Map<String, Map<String, Object[]>> filterContextMap = new LinkedHashMap<>();
      if (metricConfig != null && metricConfig.getViews() != null
          && metricConfig.getViews().size() > 0) {
        Map<String, ResultSet> viewToTEResultSet = constructViews(metricConfig.getViews());
        filterContextMap = convertToContextMap(viewToTEResultSet);
        filterSetFromView = resolveFilterSetFromView(viewToTEResultSet, request.getFilterSet());
      } else {
        filterSetFromView = request.getFilterSet();
      }

      Multimap<String, String> decoratedFilterSet = filterSetFromView;
      // Decorate filter set for pre-computed (non-additive) dataset
      // NOTE: We do not decorate the filter if the metric name is '*', which is used by count(*) query, because
      // the results are usually meta-data and should be shown regardless the filter setting.
      if (!datasetConfig.isAdditive() && !"*".equals(metricFunction.getMetricName())) {
        decoratedFilterSet =
            generateFilterSetWithPreAggregatedDimensionValue(filterSetFromView,
                request.getGroupBy(),
                datasetConfig.getDimensions(), datasetConfig.getDimensionsHaveNoPreAggregation(),
                datasetConfig.getPreAggregatedKeyword());
      }
      String sql;
      if (metricConfig != null && metricConfig.isDimensionAsMetric()) {
        sql = SqlUtils
            .getDimensionAsMetricSql(request, metricFunction, decoratedFilterSet,
                filterContextMap, dataTimeSpec
            );
      } else {
        sql = SqlUtils
            .getSql(request, metricFunction, decoratedFilterSet, filterContextMap, dataTimeSpec);
      }

      ThirdEyeResultSetGroup resultSetGroup;
      try {
        resultSetGroup = this.executeSQL(new PinotSqlQuery(sql, dataset));
      } catch (Exception e) {
        throw e;
      }

      metricFunctionToResultSetList.put(metricFunction, resultSetGroup.getResultSets());
    }

    List<String[]> resultRows = ThirdEyeResultSetUtils
        .parseResultSets(request, metricFunctionToResultSetList,
            PINOT_SQL);
    return new RelationalThirdEyeResponse(request, resultRows, timeSpec);
  }

  private Map<String, ResultSet> constructViews(List<LogicalView> views) {
    Map<String, ResultSet> viewToTEResultSet = new HashMap<>();
    for (LogicalView view : views) {
      try {
        Statement statement = this.pinotSqlResponseCacheLoader
            .getConnection().createStatement();
        ResultSet thirdEyeResultSetGroup = statement
            .executeQuery(view.getQuery());
        viewToTEResultSet.put(view.getName(), thirdEyeResultSetGroup);
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
    }
    return viewToTEResultSet;
  }

  private Map<String, Map<String, Object[]>> convertToContextMap(
      Map<String, ResultSet> viewToResultSetGroup) {
    Map<String, Map<String, Object[]>> contextMap = new LinkedHashMap<>();
    for (Map.Entry<String, ResultSet> entry : viewToResultSetGroup.entrySet()) {
      String viewName = entry.getKey();
      ResultSet resultSet = entry.getValue();
      Map<String, Object[]> columnValues = convertResultSetToMap(resultSet);
      contextMap.put(viewName, columnValues);
    }
    return contextMap;
  }

  private Map<String, Object[]> convertResultSetToMap(ResultSet resultSet) {
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int numColumns = metaData.getColumnCount();
      Map<String, Object[]> columnValues = new LinkedHashMap<>();
      for (int i = 0; i < numColumns; i++) {
        String columnName = metaData.getColumnName(i);
        String columnType = metaData.getColumnTypeName(i);
        Object[] values = getRowValues(resultSet, i, columnType);
        columnValues.put(columnName, values);
      }
      return columnValues;
    } catch (SQLException throwables) {
      return null;
    }
  }

  private Object[] getRowValues(ResultSet resultSet, int columnIndex, String columnType) {
    Object[] rowValues = null;
    try {
      resultSet.last();
      rowValues = new Object[resultSet.getRow()];
      resultSet.first();
      ColumnDataType columnDataType = ColumnDataType.valueOf(columnType);
      switch (columnDataType) {
        case INT:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getInt(columnIndex);
            resultSet.next();
          }
          break;
        case LONG:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getLong(columnIndex);
            resultSet.next();
          }
          break;
        case FLOAT:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getFloat(columnIndex);
            resultSet.next();
          }
          break;
        case DOUBLE:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getDouble(columnIndex);
            resultSet.next();
          }
          break;
        default:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = "'" + resultSet.getString(columnIndex) + "'";
            resultSet.next();
          }
          break;
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return rowValues;
  }

  private Multimap<String, String> resolveFilterSetFromView(
      Map<String, ResultSet> viewToTEResultSet, Multimap<String, String> unresolvedFilterSet) {
    Multimap<String, String> resolvedFilterSet = ArrayListMultimap.create();
    for (Map.Entry<String, String> filterEntry : unresolvedFilterSet.entries()) {
      String value = filterEntry.getValue();
      boolean isFilterOpExists = EntityUtils.isFilterOperatorExists(value);
      if (!isFilterOpExists) {
        value = EQUALS + value;
      }
      FilterPredicate filterPredicate = EntityUtils
          .extractFilterPredicate(filterEntry.getKey() + value);
      String[] fullyQualifiedColumnNameTokens = extractView(filterPredicate.getValue());

      assert fullyQualifiedColumnNameTokens.length == 2;
      String tableOrViewName = fullyQualifiedColumnNameTokens[0];
      String columnName = fullyQualifiedColumnNameTokens[1];

      if (viewToTEResultSet.containsKey(tableOrViewName)) {
        ResultSet thirdEyeResultSet = viewToTEResultSet.get(tableOrViewName);
        try {
          while (thirdEyeResultSet.next()) {
            resolvedFilterSet
                .put(filterPredicate.getKey(), thirdEyeResultSet.getString(columnName));
          }
        } catch (SQLException throwables) {
          throwables.printStackTrace();
        }
      } else {
        resolvedFilterSet.put(filterPredicate.getKey(), value);
      }
    }
    return resolvedFilterSet;
  }

  private int getIndexOfColumnName(ResultSet thirdEyeResultSet, String columnName) {
    try {
      return thirdEyeResultSet.findColumn(columnName);
    } catch (SQLException throwables) {
      return -1;
    }
  }

  private String[] extractView(String filterOrProjectExpression) {
    return filterOrProjectExpression.split(Pattern.quote("."));
  }

  /**
   * Returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotSqlQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  public ThirdEyeResultSetGroup executeSQL(PinotSqlQuery pinotSqlQuery) throws ExecutionException {
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
  public ThirdEyeResultSetGroup refreshSQL(PinotSqlQuery pinotSqlQuery) throws ExecutionException {
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

    ThirdEyeResultSet thirdEyeResultSet = executeSQL(new PinotSqlQuery(
        request.getQuery(),
        request.getTable())).get(0);
    return new ThirdEyeResultSetDataTable(thirdEyeResultSet);
  }

  @Override
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) {
    return PinotSqlDataSourceTimeQuery.getMaxDateTime(datasetConfig, this);
  }

  @Override
  public long getMinDataTime(final DatasetConfigDTO datasetConfig) {
    return PinotSqlDataSourceTimeQuery.getMinDateTime(datasetConfig, this);
  }

  @Override
  public Map<String, List<String>> getDimensionFilters(final DatasetConfigDTO datasetConfig)
      throws Exception {
    return PinotSqlDataSourceDimensionFilters.getDimensionFilters(datasetConfig, this);
  }

  @Override
  public boolean validate() {
    try {
      // Table name required to execute query against pinot broker.
      PinotDatasetOnboarder onboard = createPinotDatasetOnboarder();
      String table = onboard.getAllTables().get(0);
      String query = String.format("select 1 from %s", table);
      ThirdEyeResultSetGroup result = executeSQL(new PinotSqlQuery(query, table));
      return result.get(0).getRowCount() == 1;
    } catch (ExecutionException | IOException | ArrayIndexOutOfBoundsException e) {
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
