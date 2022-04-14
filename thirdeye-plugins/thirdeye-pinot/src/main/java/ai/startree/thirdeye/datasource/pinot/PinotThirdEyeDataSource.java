/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.auto.onboard.PinotDatasetOnboarder;
import ai.startree.thirdeye.datasource.auto.onboard.ThirdEyePinotClient;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.LogicalView;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceUtils;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.RelationalThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequestV2;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetUtils;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.util.FilterPredicate;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSet;
import org.apache.pinot.client.ResultSetGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotThirdEyeDataSource implements ThirdEyeDataSource {

  private static final String EQUALS = "=";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSource.class);
  private static final String PINOT = "Pinot";
  private static final String PINOT_QUERY_FORMAT = "pql";

  private String name;
  private PinotResponseCacheLoader pinotResponseCacheLoader;
  private LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> pinotResponseCache;
  private PinotDataSourceTimeQuery pinotDataSourceTimeQuery;
  private PinotDataSourceDimensionFilters pinotDataSourceDimensionFilters;
  private ThirdEyeDataSourceContext context;
  private SqlExpressionBuilder sqlExpressionBuilder;
  private SqlLanguage sqlLanguage;

  public PinotThirdEyeDataSource(
      final SqlExpressionBuilder sqlExpressionBuilder,
      final PinotSqlLanguage sqlLanguage) {
    this.sqlExpressionBuilder = sqlExpressionBuilder;
    this.sqlLanguage = sqlLanguage;
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

  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    this.context = context;
    final DataSourceDTO dataSourceDTO = requireNonNull(context.getDataSourceDTO(),
        "data source dto is null");

    final Map<String, Object> properties = requireNonNull(dataSourceDTO.getProperties(),
        "Data source property cannot be empty.");
    name = requireNonNull(dataSourceDTO.getName(), "name of data source dto is null");

    try {
      pinotResponseCacheLoader = new PinotControllerResponseCacheLoader();
      pinotResponseCacheLoader.init(properties);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    pinotResponseCache = DataSourceUtils.buildResponseCache(pinotResponseCacheLoader);

    // TODO Refactor. remove inverse hierarchical dependency
    pinotDataSourceTimeQuery = new PinotDataSourceTimeQuery(this);
    pinotDataSourceDimensionFilters = new PinotDataSourceDimensionFilters(this);
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
      final MetricFunction metricFunction = request.getMetricFunction();
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
            .getDimensionAsMetricSql(request, metricFunction, decoratedFilterSet,
                filterContextMap, dataTimeSpec,
                datasetConfig);
      } else {
        sql = SqlUtils
            .getSql(request, metricFunction, decoratedFilterSet, filterContextMap, dataTimeSpec);
      }

      ThirdEyeResultSetGroup resultSetGroup;
      final long tStartFunction = System.nanoTime();
      try {
        resultSetGroup = this.executeSQL(new PinotQuery(sql, dataset));
        if (metricConfig != null) {
//            RequestStatisticsLogger.getRequestLog()
//                .success(this.getName(), metricConfig.getDataset(), metricConfig.getName(),
//                    tStartFunction, System.nanoTime());
        }
      } catch (Exception e) {
        if (metricConfig != null) {
//            RequestStatisticsLogger.getRequestLog()
//                .failure(this.getName(), metricConfig.getDataset(), metricConfig.getName(),
//                    tStartFunction, System.nanoTime(), e);
        }
        throw e;
      }

      metricFunctionToResultSetList.put(metricFunction, resultSetGroup.getResultSets());

      List<String[]> resultRows = ThirdEyeResultSetUtils
          .parseResultSets(request, metricFunctionToResultSetList,
              PINOT);
      return new RelationalThirdEyeResponse(request, resultRows, timeSpec);
    } catch (Exception e) {
//      ThirdeyeMetricsUtil.pinotExceptionCounter.inc();
      throw e;
    } finally {
//      ThirdeyeMetricsUtil.pinotCallCounter.inc();
//      ThirdeyeMetricsUtil.pinotDurationCounter.inc(System.nanoTime() - tStart);
    }
  }

  private Map<String, ResultSetGroup> constructViews(List<LogicalView> views) {
    Map<String, ResultSetGroup> viewToTEResultSet = new HashMap<>();
    for (LogicalView view : views) {
      ResultSetGroup thirdEyeResultSetGroup = this.pinotResponseCacheLoader
          .getConnection()
          .execute(new Request(PINOT_QUERY_FORMAT, view.getQuery()));
      viewToTEResultSet.put(view.getName(), thirdEyeResultSetGroup);
    }
    return viewToTEResultSet;
  }

  private Map<String, Map<String, Object[]>> convertToContextMap(
      Map<String, ResultSetGroup> viewToResultSetGroup) {
    Map<String, Map<String, Object[]>> contextMap = new LinkedHashMap<>();
    for (Map.Entry<String, ResultSetGroup> entry : viewToResultSetGroup.entrySet()) {
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
    for (int i = 0; i < numColumns; i++) {
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
    if (columnType == null) {
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

  private Multimap<String, String> resolveFilterSetFromView(
      Map<String, ResultSetGroup> viewToTEResultSet, Multimap<String, String> unresolvedFilterSet) {
    Multimap<String, String> resolvedFilterSet = ArrayListMultimap.create();
    for (Map.Entry<String, String> filterEntry : unresolvedFilterSet.entries()) {
      String value = filterEntry.getValue();
      boolean isFilterOpExists = SpiUtils.isFilterOperatorExists(value);
      if (!isFilterOpExists) {
        value = EQUALS + value;
      }
      FilterPredicate filterPredicate = SpiUtils
          .extractFilterPredicate(filterEntry.getKey() + value);
      String[] fullyQualifiedColumnNameTokens = extractView(filterPredicate.getValue());

      assert fullyQualifiedColumnNameTokens.length == 2;
      String tableOrViewName = fullyQualifiedColumnNameTokens[0];
      String columnName = fullyQualifiedColumnNameTokens[1];

      if (viewToTEResultSet.containsKey(tableOrViewName)) {
        ResultSet thirdEyeResultSet = viewToTEResultSet.get(tableOrViewName).getResultSet(0);
        int columnIndex = getIndexOfColumnName(thirdEyeResultSet, columnName);

        assert columnIndex >= 0;
        for (int i = 0; i < thirdEyeResultSet.getRowCount(); i++) {
          resolvedFilterSet
              .put(filterPredicate.getKey(), thirdEyeResultSet.getString(i, columnIndex));
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

  private String[] extractView(String filterOrProjectExpression) {
    return filterOrProjectExpression.split(Pattern.quote("."));
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
    requireNonNull(this.pinotResponseCache,
        String.format("%s doesn't connect to Pinot or cache is not initialized.", getName()));

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
  public DataTable fetchDataTable(final ThirdEyeRequestV2 request) throws Exception {
    try {
      // Use pinot SQL.
      ThirdEyeResultSet thirdEyeResultSet = executeSQL(new PinotQuery(
          request.getQuery(),
          request.getTable(),
          true)).get(0);
      return new ThirdEyeResultSetDataTable(thirdEyeResultSet);
    } catch (ExecutionException e) {
      throw e;
    }
  }

  @Override
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return pinotDataSourceTimeQuery.getMaxDateTime(datasetConfig);
  }

  @Override
  public long getMinDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return pinotDataSourceTimeQuery.getMinDateTime(datasetConfig);
  }

  @Override
  public Map<String, List<String>> getDimensionFilters(final DatasetConfigDTO datasetConfig)
      throws Exception {
    return pinotDataSourceDimensionFilters.getDimensionFilters(datasetConfig);
  }

  @Override
  public boolean validate() {
    try {
      // Table name required to execute query against pinot broker.
      PinotDatasetOnboarder onboard = createPinotDatasetOnboarder();
      String table = onboard.getAllTables().get(0);
      String query = String.format("select 1 from %s", table);
      ThirdEyeResultSetGroup result = executeSQL(new PinotQuery(query, table, true));
      return result.get(0).getRowCount() == 1;
    } catch (ExecutionException | IOException | ArrayIndexOutOfBoundsException e) {
      LOG.error("Exception while performing pinot datasource validation.", e);
    }
    return false;
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
    final ThirdEyePinotClient thirdEyePinotClient = new ThirdEyePinotClient(new DataSourceMetaBean()
        .setProperties(context.getDataSourceDTO().getProperties()), "pinot");
    final PinotDatasetOnboarder pinotDatasetOnboarder = new PinotDatasetOnboarder(
        thirdEyePinotClient,
        context.getDatasetConfigManager(),
        context.getMetricConfigManager());
    return pinotDatasetOnboarder;
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
