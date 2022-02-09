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

package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.util.DataFrameUtils;
import ai.startree.thirdeye.util.RequestContainer;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAggregationLoader implements AggregationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAggregationLoader.class);

  private static final String COL_DIMENSION_NAME = "dimName";
  private static final String COL_DIMENSION_VALUE = "dimValue";
  private static final long TIMEOUT = 600000;
  private static final String ROLLUP_NAME = "OTHER";

  private final MetricConfigManager metricDAO;
  private final DatasetConfigManager datasetDAO;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DefaultAggregationLoader(MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final DataSourceCache dataSourceCache) {
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.dataSourceCache = dataSourceCache;
  }

  @Override
  public DataFrame loadBreakdown(MetricSlice slice, int limit) throws Exception {
    final long metricId = slice.getMetricId();

    // fetch meta data
    MetricConfigDTO metric = this.metricDAO.findById(metricId);
    if (metric == null) {
      throw new IllegalArgumentException(String.format("Could not resolve metric id %d", metricId));
    }

    DatasetConfigDTO dataset = this.datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve dataset '%s'", metric.getDataset()));
    }

    List<String> dimensions = new ArrayList<>(dataset.getDimensions());
    dimensions.removeAll(slice.getFilters().keySet());
    dimensions.remove(dataset.getTimeColumn());

    LOG.info("De-Aggregating '{}' for dimensions '{}'", slice, dimensions);

    DataFrame dfAll = DataFrame
        .builder(COL_DIMENSION_NAME + ":STRING", COL_DIMENSION_VALUE + ":STRING",
            DataFrame.COL_VALUE + ":DOUBLE").build()
        .setIndex(COL_DIMENSION_NAME, COL_DIMENSION_VALUE);

    Map<String, RequestContainer> requests = new HashMap<>();
    Map<String, Future<ThirdEyeResponse>> responses = new HashMap<>();

    // submit requests
    for (String dimension : dimensions) {
      RequestContainer rc = DataFrameUtils
          .makeAggregateRequest(slice, Collections.singletonList(dimension), limit, "ref",
              this.metricDAO, this.datasetDAO,
              thirdEyeCacheRegistry);
      Future<ThirdEyeResponse> res = dataSourceCache
          .getQueryResultAsync(rc.getRequest());

      requests.put(dimension, rc);
      responses.put(dimension, res);
    }

    // collect responses
    List<DataFrame> results = new ArrayList<>();
    for (String dimension : dimensions) {
      RequestContainer rc = requests.get(dimension);
      ThirdEyeResponse res = responses.get(dimension)
          .get(TIMEOUT, TimeUnit.MILLISECONDS);
      DataFrame dfRaw = DataFrameUtils.evaluateResponse(res, rc, thirdEyeCacheRegistry);
      DataFrame dfResult = new DataFrame()
          .addSeries(COL_DIMENSION_NAME, StringSeries.fillValues(dfRaw.size(), dimension))
          .addSeries(COL_DIMENSION_VALUE, dfRaw.get(dimension))
          .addSeries(DataFrame.COL_VALUE, dfRaw.get(DataFrame.COL_VALUE));
      results.add(dfResult);
    }

    final DataFrame breakdown = dfAll.append(results);
    // add time column containing start time of slice
    return breakdown
        .addSeries(DataFrame.COL_TIME, LongSeries.fillValues(breakdown.size(), slice.getStart()))
        .setIndex(DataFrame.COL_TIME, COL_DIMENSION_NAME, COL_DIMENSION_VALUE);
  }

  @Override
  public DataFrame loadAggregate(MetricSlice slice, List<String> dimensions, int limit)
      throws Exception {
    final long metricId = slice.getMetricId();

    // fetch meta data
    MetricConfigDTO metric = this.metricDAO.findById(metricId);
    if (metric == null) {
      throw new IllegalArgumentException(String.format("Could not resolve metric id %d", metricId));
    }

    DatasetConfigDTO dataset = this.datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve dataset '%s'", metric.getDataset()));
    }

    LOG.info("Aggregating '{}'", slice);

    List<String> cols = new ArrayList<>();
    for (String dimName : dimensions) {
      cols.add(dimName + ":STRING");
    }
    cols.add(DataFrame.COL_VALUE + ":DOUBLE");

    DataFrame dfEmpty = DataFrame.builder(cols).build().setIndex(dimensions);

    final long maxTime = thirdEyeCacheRegistry.getDatasetMaxDataTimeCache()
        .get(dataset.getDataset());
    if (slice.getStart() > maxTime) {
      return dfEmpty;
    }

    RequestContainer rc = DataFrameUtils
        .makeAggregateRequest(slice, new ArrayList<>(dimensions), limit, "ref", this.metricDAO,
            this.datasetDAO, thirdEyeCacheRegistry);
    ThirdEyeResponse res = dataSourceCache
        .getQueryResult(rc.getRequest());
    final DataFrame aggregate = DataFrameUtils.evaluateResponse(res, rc, thirdEyeCacheRegistry);

    // fill in timestamps
    return aggregate
        .addSeries(DataFrame.COL_TIME, LongSeries.fillValues(aggregate.size(), slice.getStart()))
        .setIndex(DataFrame.COL_TIME);
  }

  /**
   * Returns a map of maps (keyed by dimension name, keyed by dimension value) derived from the
   * breakdown results dataframe.
   *
   * @param dataBreakdown (transformed) breakdown query results
   * @param dataAggregate (transformed) aggregate query results
   * @return map of maps of value (keyed by dimension name, keyed by dimension value)
   */
  public static Map<String, Map<String, Double>> makeBreakdownMap(DataFrame dataBreakdown,
      DataFrame dataAggregate) {
    Map<String, Map<String, Double>> output = new TreeMap<>();

    dataBreakdown = dataBreakdown.dropNull();
    dataAggregate = dataAggregate.dropNull();

    Map<String, Double> dimensionTotals = new HashMap<>();

    for (int i = 0; i < dataBreakdown.size(); i++) {
      final String dimName = dataBreakdown.getString(COL_DIMENSION_NAME, i);
      final String dimValue = dataBreakdown.getString(COL_DIMENSION_VALUE, i);
      final double value = dataBreakdown.getDouble(DataFrame.COL_VALUE, i);

      // cell
      if (!output.containsKey(dimName)) {
        output.put(dimName, new HashMap<>());
      }
      output.get(dimName).put(dimValue, value);

      // total
      dimensionTotals.put(dimName, MapUtils.getDoubleValue(dimensionTotals, dimName, 0) + value);
    }

    // add rollup column
    if (!dataAggregate.isEmpty()) {
      double total = dataAggregate.getDouble(DataFrame.COL_VALUE, 0);
      for (Map.Entry<String, Double> entry : dimensionTotals.entrySet()) {
        if (entry.getValue() < total) {
          output.get(entry.getKey()).put(ROLLUP_NAME, total - entry.getValue());
        }
      }
    }

    return output;
  }
}
