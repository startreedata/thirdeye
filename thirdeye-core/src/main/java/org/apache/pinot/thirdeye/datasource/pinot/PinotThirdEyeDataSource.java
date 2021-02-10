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

package org.apache.pinot.thirdeye.datasource.pinot;

import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSet;
import org.apache.pinot.client.ResultSetGroup;
import org.apache.pinot.common.utils.DataSchema.ColumnDataType;
import org.apache.pinot.thirdeye.anomaly.utils.ThirdeyeMetricsUtil;
import org.apache.pinot.thirdeye.common.time.TimeSpec;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.LogicalView;
import org.apache.pinot.thirdeye.datasource.MetricFunction;
import org.apache.pinot.thirdeye.datasource.RelationalQuery;
import org.apache.pinot.thirdeye.datasource.RelationalThirdEyeResponse;
import org.apache.pinot.thirdeye.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSet;
import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSetUtils;
import org.apache.pinot.thirdeye.rootcause.util.EntityUtils;
import org.apache.pinot.thirdeye.rootcause.util.FilterPredicate;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotThirdEyeDataSource implements ThirdEyeDataSource {

  private static final String EQUALS = "=";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSource.class);
  private static final String PINOT = "Pinot";
  private static final String PINOT_QUERY_FORMAT = "pql";

  private String name;

  private static final long CONNECTION_TIMEOUT = 60000;

  public static final String CACHE_LOADER_CLASS_NAME_STRING = "cacheLoaderClassName";
  protected LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> pinotResponseCache;

  protected PinotDataSourceTimeQuery pinotDataSourceTimeQuery;
  protected PinotDataSourceDimensionFilters pinotDataSourceDimensionFilters;
  private PinotResponseCacheLoader pinotResponseCacheLoader;

  /**
   * Construct a Pinot data source, which connects to a Pinot controller, using {@link
   * PinotThirdEyeDataSourceConfig}.
   *
   * @param pinotThirdEyeDataSourceConfig the configuration that provides the information of the
   *     Pinot controller.
   * @throws Exception when failed to connect to the controller.
   */
  public PinotThirdEyeDataSource(PinotThirdEyeDataSourceConfig pinotThirdEyeDataSourceConfig)
      throws Exception {
    pinotResponseCacheLoader = new PinotControllerResponseCacheLoader(
        pinotThirdEyeDataSourceConfig);
    pinotResponseCache = ThirdEyeUtils.buildResponseCache(pinotResponseCacheLoader);

    pinotDataSourceTimeQuery = new PinotDataSourceTimeQuery(this);
    pinotDataSourceDimensionFilters = new PinotDataSourceDimensionFilters(this);
    name = pinotThirdEyeDataSourceConfig.getName() != null ? pinotThirdEyeDataSourceConfig.getName()
        : PinotThirdEyeDataSource.class.getSimpleName();
  }

  /**
   * This constructor is invoked by Java Reflection for initialize a ThirdEyeDataSource.
   *
   * @param properties the property to initialize this data source.
   */
  public PinotThirdEyeDataSource(Map<String, Object> properties) throws Exception {
    Preconditions.checkNotNull(properties, "Data source property cannot be empty.");

    pinotResponseCacheLoader = getCacheLoaderInstance(properties);
    pinotResponseCacheLoader.init(properties);
    pinotResponseCache = ThirdEyeUtils.buildResponseCache(pinotResponseCacheLoader);

    pinotDataSourceTimeQuery = new PinotDataSourceTimeQuery(this);
    pinotDataSourceDimensionFilters = new PinotDataSourceDimensionFilters(this);
    name = MapUtils.getString(properties, "name", PinotThirdEyeDataSource.class.getSimpleName());
  }

  /**
   * Constructs a PinotResponseCacheLoader from the given property map and initialize the loader
   * with that map.
   *
   * @param properties the property map of the cache loader, which contains the class path of
   *     the cache loader.
   * @return a constructed PinotResponseCacheLoader.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  static PinotResponseCacheLoader getCacheLoaderInstance(Map<String, Object> properties)
      throws Exception {
    final String cacheLoaderClassName;
    if (properties.containsKey(CACHE_LOADER_CLASS_NAME_STRING)) {
      cacheLoaderClassName = properties.get(CACHE_LOADER_CLASS_NAME_STRING).toString();
    } else {
      cacheLoaderClassName = PinotControllerResponseCacheLoader.class.getName();
    }
    LOG.info("Constructing cache loader: {}", cacheLoaderClassName);
    Class<?> aClass = null;
    try {
      aClass = Class.forName(cacheLoaderClassName);
    } catch (Throwable throwable) {
      LOG.error("Failed to initiate cache loader: {}; reason:", cacheLoaderClassName, throwable);
      aClass = PinotControllerResponseCacheLoader.class;
    }
    LOG.info("Initiating cache loader: {}", aClass.getName());
    Constructor<?> constructor = aClass.getConstructor();
    PinotResponseCacheLoader pinotResponseCacheLoader = (PinotResponseCacheLoader) constructor
        .newInstance();
    return pinotResponseCacheLoader;
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

    long tStart = System.nanoTime();
    try {
      LinkedHashMap<MetricFunction, List<ThirdEyeResultSet>> metricFunctionToResultSetList = new LinkedHashMap<>();

      TimeSpec timeSpec = null;
      for (MetricFunction metricFunction : request.getMetricFunctions()) {
        String dataset = metricFunction.getDataset();
        DatasetConfigDTO datasetConfig = ThirdEyeUtils.getDatasetConfigFromName(dataset);
        TimeSpec dataTimeSpec = ThirdEyeUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);
        if (timeSpec == null) {
          timeSpec = dataTimeSpec;
        }

        MetricConfigDTO metricConfig = metricFunction.getMetricConfig();
        Multimap<String, String> filterSetFromView;
        Map<String, Map<String, Object[]>> filterContextMap = new LinkedHashMap<>();
        if(metricConfig != null && metricConfig.getViews() != null && metricConfig.getViews().size() > 0) {
          Map<String, ResultSetGroup> viewToTEResultSet = constructViews(metricConfig.getViews());
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
              .getDimensionAsMetricSql(request, metricFunction, decoratedFilterSet, filterContextMap, dataTimeSpec,
                  datasetConfig);
        } else {
          sql = SqlUtils.getSql(request, metricFunction, decoratedFilterSet, filterContextMap, dataTimeSpec);
        }

        ThirdEyeResultSetGroup resultSetGroup;
        final long tStartFunction = System.nanoTime();
        try {
          resultSetGroup = this.executeSQL(new PinotQuery(sql, dataset));
          if (metricConfig != null) {
            ThirdeyeMetricsUtil.getRequestLog()
                .success(this.getName(), metricConfig.getDataset(), metricConfig.getName(),
                    tStartFunction, System.nanoTime());
          }
        } catch (Exception e) {
          if (metricConfig != null) {
            ThirdeyeMetricsUtil.getRequestLog()
                .failure(this.getName(), metricConfig.getDataset(), metricConfig.getName(),
                    tStartFunction, System.nanoTime(), e);
          }
          throw e;
        }

        metricFunctionToResultSetList.put(metricFunction, resultSetGroup.getResultSets());
      }

      List<String[]> resultRows = ThirdEyeResultSetUtils
          .parseResultSets(request, metricFunctionToResultSetList,
              PINOT);
      return new RelationalThirdEyeResponse(request, resultRows, timeSpec);
    } catch (Exception e) {
      ThirdeyeMetricsUtil.pinotExceptionCounter.inc();
      throw e;
    } finally {
      ThirdeyeMetricsUtil.pinotCallCounter.inc();
      ThirdeyeMetricsUtil.pinotDurationCounter.inc(System.nanoTime() - tStart);
    }
  }

  private Map<String, ResultSetGroup> constructViews(List<LogicalView> views) throws Exception {
    Map<String, ResultSetGroup> viewToTEResultSet = new HashMap<>();
    for(LogicalView view : views) {
      ResultSetGroup thirdEyeResultSetGroup = this.pinotResponseCacheLoader
              .getConnection()
              .execute(new Request(PINOT_QUERY_FORMAT, view.getQuery()));
      viewToTEResultSet.put(view.getName(), thirdEyeResultSetGroup);
    }
    return viewToTEResultSet;
  }

  private Map<String, Map<String, Object[]>> convertToContextMap(Map<String, ResultSetGroup> viewToResultSetGroup) {
    Map<String, Map<String, Object[]>> contextMap = new LinkedHashMap<>();
    for(Map.Entry<String, ResultSetGroup> entry: viewToResultSetGroup.entrySet()) {
      String viewName = entry.getKey();
      ResultSetGroup resultSetGroup = entry.getValue();
      ResultSet resultSet = resultSetGroup.getResultSet(0);
      Map<String, Object[]> columnValues = convertResultSetToMap(resultSet);
      contextMap.put(viewName, columnValues);
    }
    return contextMap;
  }

  private Map<String, Object[]> convertResultSetToMap(ResultSet resultSet) {
    int numColumns = resultSet.getColumnCount();
    Map<String, Object[]> columnValues = new LinkedHashMap<>();
    for(int i=0; i<numColumns; i++) {
      String columnName = resultSet.getColumnName(i);
      String columnType = resultSet.getColumnDataType(i);
      Object[] values = getRowValues(resultSet, i, columnType);
      columnValues.put(columnName, values);
    }
    return columnValues;
  }

  private Object[] getRowValues(ResultSet resultSet, int columnIndex, String columnType) {
    Object[] rowValues = new Object[resultSet.getRowCount()];
    //TODO: This check needs to be removed once the resultset.getColumnType(i) returns not null
    // The changes need to be made in pinot-java-client
    if(columnType == null ) {
      for (int i = 0; i < rowValues.length; i++) {
        rowValues[i] = "'" + resultSet.getString(i, columnIndex) + "'";
      }
    } else {
      ColumnDataType columnDataType = ColumnDataType.valueOf(columnType);
      switch (columnDataType) {
        case INT:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getInt(i, columnIndex);
          }
          break;
        case LONG:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getLong(i, columnIndex);
          }
          break;
        case FLOAT:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getFloat(i, columnIndex);
          }
          break;
        case DOUBLE:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = resultSet.getDouble(i, columnIndex);
          }
          break;
        default:
          for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = "'" + resultSet.getString(i, columnIndex) + "'";
          }
          break;
      }
    }
    return rowValues;
  }

  private Multimap<String, String> resolveFilterSetFromView(Map<String, ResultSetGroup> viewToTEResultSet, Multimap<String, String> unresolvedFilterSet) {
    Multimap<String, String> resolvedFilterSet =  ArrayListMultimap.create();
    for(Map.Entry<String, String> filterEntry : unresolvedFilterSet.entries()) {
      String value = filterEntry.getValue();
      boolean isFilterOpExists = EntityUtils.isFilterOperatorExists(value);
      if(!isFilterOpExists) {
        value = EQUALS + value;
      }
      FilterPredicate filterPredicate = EntityUtils.extractFilterPredicate(filterEntry.getKey() + value);
      String[] fullyQualifiedColumnNameTokens = extractView(filterPredicate.getValue());

      assert fullyQualifiedColumnNameTokens.length == 2;
      String tableOrViewName = fullyQualifiedColumnNameTokens[0];
      String columnName = fullyQualifiedColumnNameTokens[1];
      
      if(viewToTEResultSet.containsKey(tableOrViewName)) {
        ResultSet thirdEyeResultSet = viewToTEResultSet.get(tableOrViewName).getResultSet(0);
        int columnIndex = getIndexOfColumnName(thirdEyeResultSet, columnName);

        assert columnIndex >= 0;
        for(int i=0; i<thirdEyeResultSet.getRowCount(); i++) {
          resolvedFilterSet.put(filterPredicate.getKey(), thirdEyeResultSet.getString(i, columnIndex));
        }
      } else {
          resolvedFilterSet.put(filterPredicate.getKey(), value);
      }
    }
    return resolvedFilterSet;
  }

  private int getIndexOfColumnName(ResultSet thirdEyeResultSet, String columnName) {
    int count = 0;
    while (count < thirdEyeResultSet.getColumnCount()) {
      if (thirdEyeResultSet.getColumnName(count).equalsIgnoreCase(columnName)) {
        return count;
      }
      count++;
    }
    return -1;
  }

  private  String[] extractView(String filterOrProjectExpression) {
    return filterOrProjectExpression.split(Pattern.quote("."));
  }

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

  /**
   * Returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  public ThirdEyeResultSetGroup executeSQL(PinotQuery pinotQuery) throws ExecutionException {
    Preconditions
        .checkNotNull(this.pinotResponseCache,
            "{} doesn't connect to Pinot or cache is not initialized.", getName());

    try {
      return this.pinotResponseCache.get(pinotQuery);
    } catch (ExecutionException e) {
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
  public ThirdEyeResultSetGroup refreshSQL(PinotQuery pinotQuery) throws ExecutionException {
    Preconditions
        .checkNotNull(this.pinotResponseCache,
            "{} doesn't connect to Pinot or cache is not initialized.", getName());

    try {
      pinotResponseCache.refresh(pinotQuery);
      return pinotResponseCache.get(pinotQuery);
    } catch (ExecutionException e) {
      LOG.error("Failed to refresh PQL: {}", pinotQuery.getQuery());
      throw e;
    }
  }

  @Override
  public List<String> getDatasets() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return pinotDataSourceTimeQuery.getMaxDateTime(
        datasetConfig);
  }

  @Override
  public long getMinDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return pinotDataSourceTimeQuery.getMinDateTime(datasetConfig);
  }

  @Override
  public Map<String, List<String>> getDimensionFilters(String dataset) throws Exception {
    return pinotDataSourceDimensionFilters.getDimensionFilters(dataset);
  }

  @Override
  public void clear() throws Exception {
  }

  @Override
  public void close() throws Exception {
    controllerClient.close();
  }

  /**
   * TESTING ONLY - WE SHOULD NOT BE USING THIS.
   */
  @Deprecated
  private HttpHost controllerHost;
  @Deprecated
  private CloseableHttpClient controllerClient;

  @Deprecated
  protected PinotThirdEyeDataSource(String host, int port) {
    this.controllerHost = new HttpHost(host, port);
    this.controllerClient = HttpClients.createDefault();
    this.pinotDataSourceTimeQuery = new PinotDataSourceTimeQuery(this);
    this.pinotDataSourceDimensionFilters = new PinotDataSourceDimensionFilters(this);
    LOG.info("Created PinotThirdEyeDataSource with controller {}", controllerHost);
  }

  @Deprecated
  public static PinotThirdEyeDataSource fromZookeeper(String controllerHost, int controllerPort,
      String zkUrl) {
    ZkClient zkClient = new ZkClient(zkUrl);
    zkClient.setZkSerializer(new ZNRecordSerializer());
    zkClient.waitUntilConnected(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
    PinotThirdEyeDataSource pinotThirdEyeDataSource = new PinotThirdEyeDataSource(controllerHost,
        controllerPort);
    LOG.info("Created PinotThirdEyeDataSource to zookeeper: {} controller: {}:{}", zkUrl,
        controllerHost, controllerPort);
    return pinotThirdEyeDataSource;
  }

  @Deprecated
  public static PinotThirdEyeDataSource getDefaultTestDataSource() {
    // TODO REPLACE WITH CONFIGS
    String controllerHost = "localhost";
    int controllerPort = 10611;
    String zkUrl = "localhost:12913/pinot-cluster";
    return fromZookeeper(controllerHost, controllerPort, zkUrl);
  }
}
