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

package org.apache.pinot.thirdeye.datasource.loader;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.StringSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.util.DataFrameUtils;
import org.apache.pinot.thirdeye.util.RequestContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAggregationLoader implements AggregationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAggregationLoader.class);

  private static final long TIMEOUT = 600000;

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

  private static long makeTimeout(long deadline) {
    return Math.max(deadline - System.currentTimeMillis(), 0);
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
    final long deadline = System.currentTimeMillis() + TIMEOUT;

    List<DataFrame> results = new ArrayList<>();
    for (String dimension : dimensions) {
      RequestContainer rc = requests.get(dimension);
      ThirdEyeResponse res = responses.get(dimension)
          .get(makeTimeout(deadline), TimeUnit.MILLISECONDS);
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
}
